#!/usr/bin/python

from subprocess import check_output
from subprocess import Popen
from time import sleep
from time import time

# from datetime import timedelta
from typing import Callable

from subprocess import PIPE

TAG = "OuinetInstrumentedTest"
RUNNING = "RUNNING"
SESSION_TIMEOUT = 60  # seconds
TEST_ALLOWED_TIME = 5 * 60  # seconds


def wait_unless_timeout(check: Callable, timeout_s: int) -> None:
    started = time()
    while not check():
        elapsed = time() - started
        if elapsed > timeout_s:
            break
        sleep(1)
        print(".", end="", flush=True)
    if not check():
        raise TimeoutError("Timed out on", check)


def bash_thread(command: str) -> str:
    return Popen(["bash", "-c", command], stdout=PIPE, stderr=PIPE)


def bash(command: str) -> str:
    return check_output(["bash", "-c", command], text=True)


def waydroid_shell(command: str) -> str:
    return check_output(
        ["sudo", "waydroid", "shell", "--", "sh", "-c", command], text=True
    )


def is_boot_done() -> bool:
    boot_completed_str = waydroid_shell("getprop sys.boot_completed")
    result = boot_completed_str.strip() == "1"
    return result


def get_status_field(field: str):
    output = check_output(["waydroid", "status"], text=True)
    for line in output.splitlines():
        if field in line:
            return line.split(field + ":")[-1].strip()
    raise IOError("No field ", field, "in output:", output)


def get_status() -> str:
    return get_status_field("Session")


def get_ip() -> str:
    return get_status_field("IP address")


def session_ready() -> bool:
    return get_status() == RUNNING


def ensure_session() -> Popen:
    print("Checking Waydroid session...")
    if session_ready():
        print("Waydroid session in place. stopping")
        bash("waydroid session stop")

    if session_ready():
        raise Exception("Waydroid session refuses to stop")

    print("Starting new waydroid session")

    process = bash_thread("waydroid session start")
    print("Waiting for Waydroid session to start...")
    try:
        wait_unless_timeout(session_ready, SESSION_TIMEOUT)
    except TimeoutError as e:
        raise Exception("Could not obtain session") from e
    return process


def ensure_android():
    print("Waiting for Waydroid Android to finish booting...")
    wait_unless_timeout(is_boot_done, SESSION_TIMEOUT)
    print("Android is booted")

    ensure_adbd()


def ensure_adbd():
    print("Preparing adbd...")
    waydroid_shell("setprop service.adb.tcp.port 5555")
    print("stopping adbd...")
    waydroid_shell("stop adbd")
    print("starting adbd...")
    waydroid_shell("start adbd")
    print("starting adbd one more time to make sure it is done...")
    waydroid_shell("start adbd")


def assert_waydroid():
    try:
        print("Using Waydroid", bash("waydroid --version").strip())
    except Exception as e:
        raise Exception("We do not appear to have usable Waydroid") from e


def assert_adb():
    try:
        output = bash("adb version")
        assert output
        firstline = output.splitlines()[0].strip()
        print("Using", firstline)
    except Exception as e:
        raise Exception("We do not appear to have usable ADB") from e


def restart_host_adb():
    print("Host adb preparation:")
    print("\t* killing host adb")
    bash("adb kill-server")
    print("\t* starting host adb")
    bash("adb start-server")


def authorize():
    try:
        pubkey = bash("cat ~/.android/adbkey.pub")
    except Exception as e:
        raise Exception(
            "Unable to retrieve public key. Check if you have ~/.android, which is a part of waydroid setup"
        ) from e

    keypath = "/data/misc/adb/adb_keys"
    print("making the keyfile in the android")
    waydroid_shell(f"touch {keypath}")
    print("filling the keyfile")
    waydroid_shell(f"echo {pubkey} > {keypath}")
    print("hopefully is authorized now")


def connect():
    try:
        ip = get_ip()
        assert ip
    except:
        raise Exception("Failed to detect Waydroid IP address")

    waydroid_ip = get_ip()

    print(f"Connecting host's ADB to {waydroid_ip}:5555")

    bash(f"adb connect {waydroid_ip}:5555")


def devices() -> str:
    return bash("adb devices")


def ensure_device():
    devs = devices()
    if not devs:
        raise IndexError("no devices available!")

    if "unauthorized" in devs.lower():
        print("Device unauthorized. Trying to authorize")
        authorize()
        ensure_adbd()
        connect()
        if "unauthorized" in devices().lower():
            raise Exception("We tried to authorize the device but failed!")

        print("authorized")
        return
    else:
        print(f"Available devices: {devs}")
        print("Waydroid is now ready for ADB")


def do_test() -> Popen:
    return Popen(
        [
            "./gradlew",
            "connectedAndroidTest",
            "-Pandroid.testInstrumentationRunnerArguments.clearPackageData=true",
            "-Pandroid.testInstrumentationRunnerArguments.disableComposedFailuresReporting=true",
            "--no-continue",
            "-Dtest.output.console=true",
        ],
    )


def get_live_log() -> Popen:
    process = Popen(
        ["bash", "-c", f"sudo waydroid logcat -- -v threadtime | grep -i {TAG}"]
    )
    return process


def main():
    assert_waydroid()
    assert_adb()
    session = ensure_session()
    log = get_live_log()

    try:
        ensure_android()
        restart_host_adb()
        connect()
        ensure_device()

        gradle = do_test()
        gradle.wait(TEST_ALLOWED_TIME)

    finally:
        print("stopping log reading")
        log.kill()
        print("Killing session")
        session.kill()
        bash("waydroid session stop")

        print("stopping adb")
        bash("adb kill-server")


main()

"""Exercise the generated Kotlin REST starter on its full JPMS module path.

First run Maven verify and dependency:copy-dependencies -DoutputDirectory=target/runtime
-DincludeScope=runtime on build/generated-kotlin/maven/pom.xml.
"""
from pathlib import Path
import os
import socket
import subprocess
import time
import urllib.request
import urllib.error

root = Path(__file__).resolve().parents[1] / 'build/generated-kotlin/maven'
with socket.socket() as listener:
    listener.bind(('127.0.0.1', 0))
    port = listener.getsockname()[1]
env = dict(os.environ, HTTP_PORT=str(port), VERTX_EVENTBUS_CLUSTER_PUBLIC_PORT='0',
           VERTX_CLUSTER_ENABLED='false', HAZELCAST_START_LOCAL='false', HAZELCAST_CLIENT_ENABLED='false')
java = str(Path(env['JAVA_HOME']) / 'bin/java') if env.get('JAVA_HOME') else 'java'
log = root / 'target/kotlin-runtime.log'
with log.open('w') as output:
    proc = subprocess.Popen([java, '--module-path', os.pathsep.join([str(root/'target/classes'), str(root/'target/runtime')]),
        '-m', 'kotlin.app/example.kotlin.WebApplication'], env=env, stdout=output, stderr=subprocess.STDOUT, cwd=root)
    try:
        deadline = time.monotonic() + 45
        while time.monotonic() < deadline:
            if proc.poll() is not None: raise RuntimeError(f'Application exited {proc.returncode}; see {log}')
            try:
                with urllib.request.urlopen(f'http://127.0.0.1:{port}/resources', timeout=1) as response:
                    body = response.read().decode()
                    assert response.status == 200 and 'Sample data from RestResourceService' in body, body
                    print('PASS: Kotlin REST resource and injected service respond over HTTP on the full module path')
                    break
            except urllib.error.HTTPError as error:
                if error.code != 404: raise
                time.sleep(.25)
            except OSError:
                time.sleep(.25)
        else: raise TimeoutError(f'Kotlin REST endpoint did not become ready; see {log}')
    finally:
        proc.terminate()
        try: proc.wait(timeout=10)
        except subprocess.TimeoutExpired: proc.kill(); proc.wait()

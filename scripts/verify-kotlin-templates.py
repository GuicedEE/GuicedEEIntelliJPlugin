"""Render every Kotlin file template into a Maven compilation fixture.

Run with Python, then Maven 4 -f build/kotlin-template-fixture/pom.xml verify.
This checks the real template text against the locally installed 2.6.0 APIs.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
out = root / 'build/kotlin-template-fixture'
out.mkdir(parents=True, exist_ok=True)
resources = root / 'src/main/resources'
templates = sorted((resources / 'fileTemplates/j2ee').glob('*.kt.ft'))
for template in templates:
    stem = template.name.removesuffix('.kt.ft')
    name = stem.removeprefix('GuicedEE').removeprefix('GuiceNew') + 'Example'
    package = 'fixture.' + name.lower()
    values = dict(PACKAGE_NAME=package, NAME=name, SERVICE_NAME=name + 'Service',
                  REST_SERVICE_NAME=name, INTERFACE_NAME=name + 'Contract', PATH='example',
                  MODULE_NAME='fixture.app', PERSISTENCE_UNIT='postgres', DATABASE_TYPE='postgresql',
                  CONNECTION_NAME='default', KEYSPACE='application', DATABASE_NAME='application',
                  QUEUE_NAME='example', TOPIC_NAME='example', REDIS_HOST='localhost', REDIS_PORT='6379',
                  REDIS_DATABASE='0', REDIS_NAME='default', MAX_POOL_SIZE='8', GRPC_NAME='default',
                  GRPC_HOST='localhost', GRPC_PORT='9090', PROXY_NAME='default', PROXY_PORT='8080',
                  ORIGIN_HOST='localhost', ORIGIN_PORT='8081')
    text = template.read_text(encoding='utf-8')
    for key, value in values.items(): text = text.replace('${' + key + '}', value)
    assert '${' not in text, (template, re.findall(r'\$\{.*?\}', text))
    dest = out / 'src/main/kotlin' / Path(*package.split('.')) / (name + '.kt')
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(text, encoding='utf-8')
    if stem in ('GuicedEERestService', 'GuicedEERestServiceWithSession'):
        service = 'GuicedEERestServiceImpl' + ('WithSessionParam' if stem.endswith('WithSession') else '')
        body = (resources / ('fileTemplates/j2ee/' + service + '.kt.ft')).read_text()
        for key, value in values.items(): body = body.replace('${' + key + '}', value)
        dest.with_name(name + 'Service.kt').write_text(body)
    if stem == 'GuicedEESoapServiceImpl':
        dest.with_name(name + 'Contract.kt').write_text('package ' + package + '\ninterface ' + name + 'Contract { fun hello(name: String): String }\n')
deps = ['kotlin','inject','rest','rest-client','persistence','websockets','rabbitmq','kafka','ibmmq','graphql',
        'hazelcast','health','vertx','mail-client','web-services','runtime-autoconfigure','service-registry']
plugins = (resources / 'kotlin-starter/maven-plugins.xml.txt').read_text()
(out / 'pom.xml').write_text('''<project xmlns="http://maven.apache.org/POM/4.0.0">
<modelVersion>4.0.0</modelVersion><groupId>fixture</groupId><artifactId>kotlin-templates</artifactId><version>1</version>
<properties><maven.compiler.release>25</maven.compiler.release><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding></properties>
<dependencyManagement><dependencies><dependency><groupId>com.guicedee</groupId><artifactId>guicedee-bom</artifactId><version>2.6.0</version><type>pom</type><scope>import</scope></dependency></dependencies></dependencyManagement>
<dependencies>''' + ''.join('<dependency><groupId>com.guicedee</groupId><artifactId>' + dep + '</artifactId><version>2.6.0</version></dependency>' for dep in deps) +
'''</dependencies><build><plugins>''' + plugins + '''</plugins></build></project>''')
print(f'Rendered {len(templates)} Kotlin templates into {out}')


## Test run from hudson integration system.
## Run example:
##  ${WORKSPACE}/code/src/test/resources/integration.sh ${BUILD_NUMBER} ${WORKSPACE} 

## Start graphic environment emulator to start firefox for selenium integration tests
startx -- `which Xvfb` :1 -screen 0 1024x768x24 2>&1 >/dev/null &
export DISPLAY=:1

## Remove old potential temporal folder of previous test, create new one and go into
rm -rf /tmp/gvnixtest*
mkdir /tmp/gvnixtest$1
cd /tmp/gvnixtest$1

## Roo bikeshop
$2/code/bin/gvnix-dev script --file bikeshop.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## Roo clinic
$2/code/bin/gvnix-dev script --file clinic.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## Roo embedding
$2/code/bin/gvnix-dev script --file embedding.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## Roo expenses
$2/code/bin/gvnix-dev script --file expenses.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## Roo multimodule
$2/code/bin/gvnix-dev script --file multimodule.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## Roo pizzashop
$2/code/bin/gvnix-dev script --file pizzashop.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## Roo vote
$2/code/bin/gvnix-dev script --file vote.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## Roo wedding
$2/code/bin/gvnix-dev script --file wedding.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX binding add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-mvc-binding/src/main/resources/binding.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX configuration add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-dynamic-configuration/src/main/resources/configuration.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX configuration add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-dynamic-configuration/src/main/resources/configuration.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX dialog add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-dialog/src/main/resources/dialog.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX flex add-on
$2/code/bin/gvnix-dev script --file $2/code/spring-flex-roo/org.springframework.flex.roo.addon/src/test/resources/basic-flex-scaffold-test.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX flex add-on
$2/code/bin/gvnix-dev script --file $2/code/spring-flex-roo/org.springframework.flex.roo.addon/src/test/resources/remoting-scaffold-all-test.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX flex add-on
$2/code/bin/gvnix-dev script --file $2/code/spring-flex-roo/org.springframework.flex.roo.addon/src/main/resources/flex.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX flex add-on
$2/code/bin/gvnix-dev script --file $2/code/spring-flex-roo/rootunes.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX i18n add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-i18n/src/main/resources/es-i18n.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX menu add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-menu/src/main/resources/menu.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX menu add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-menu/src/test/resources/base.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX occ add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-occ/src/main/resources/occ.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX pattern add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-pattern/src/main/resources/pattern.roo
$2/code/bin/gvnix-dev hint
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX pattern add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc-roo
$2/code/bin/gvnix-dev hint
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX pattern add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc2-roo
$2/code/bin/gvnix-dev hint
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX pattern add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-pattern/src/test/resources/test-script-pkc3-roo
$2/code/bin/gvnix-dev hint
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX pattern add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-pattern/src/test/resources/test-script-manytomany-roo
$2/code/bin/gvnix-dev hint
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX report add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-report/src/main/resources/report.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX report add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-web-report/src/test/resources/gvnix-test.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX service add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-service/src/main/resources/bing.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX service add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-service/src/main/resources/service.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX service add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-service/src/test/resources/gvnix-test-no-web.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX service add-on
$2/code/bin/gvnix-dev script --file $2/code/addon-service/src/test/resources/gvnix-test.roo
mvn test tomcat:run -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX theme add-on 
$2/code/bin/gvnix-dev script --file $2/code/addon-web-theme/src/main/resources/theme.roo
rm -rf /tmp/gvnixtest$1/*

## gvNIX add-ons
$2/code/bin/gvnix-dev script --file $2/code/src/main/assembly/samples/gvnix-sample.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX add-ons
$2/code/bin/gvnix-dev script --file $2/code/src/test/resources/script.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

## gvNIX add-ons
$2/code/bin/gvnix-dev script --file $2/code/src/test/resources/tiendavirtual.roo
mvn test tomcat:run selenium:xvfb selenium:selenese -Dmaven.tomcat.fork=true 
rm -rf /tmp/gvnixtest$1/*

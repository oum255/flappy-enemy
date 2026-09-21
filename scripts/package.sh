#!/usr/bin/env bash
# Construit une application autonome (avec sa propre JVM, rien à installer pour la lancer) dans target/dist.
# Prérequis : JDK 21 ou plus (fournit jpackage) et Maven.
set -euo pipefail
cd "$(dirname "$0")/.."

# jpackage attend ';' comme séparateur de chemins sous Windows, ':' ailleurs ; chaque système a son format d'icône
case "$(uname -s)" in
  Darwin) SEP=":"; ICON="packaging/icon.icns" ;;
  MINGW*|MSYS*|CYGWIN*) SEP=";"; ICON="packaging/icon.ico" ;;
  *) SEP=":"; ICON="packaging/icon.png" ;;
esac

mvn -B -DskipTests package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/libs

# Maven fournit deux jars par module JavaFX (avec et sans classifieur de plateforme) : les deux déclarent
# le même module, on ne garde donc que celui de la plateforme courante.
for jar in target/libs/javafx-*.jar; do
  name=$(basename "$jar" .jar)
  if [[ "$name" =~ ^javafx-[a-z]+-[0-9.]+$ ]]; then
    rm "$jar"
  fi
done

JAR=$(ls target/flappy-enemy-*.jar | head -n 1)
rm -rf target/dist

jpackage --type app-image \
  --name FlappyEnemy \
  --icon "$ICON" \
  --module-path "${JAR}${SEP}target/libs" \
  --module flappyenemy/com.flappyenemy.view.FlappyEnemy \
  --dest target/dist

echo "App created in target/dist"

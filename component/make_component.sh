#!/usr/bin/env bash
set -e

COMPONENT_DIR="component_temp_dir"
LANGUAGE_PATH="$COMPONENT_DIR/jre/languages/sl"
SIMPLE_LANGUAGE_JAR="../language/target/simplelanguage.jar"
if [[ -f ../native/slnative ]]; then
    INCLUDE_SLNATIVE="TRUE"
fi

if [[ -d $COMPONENT_DIR ]]; then
	local user_input
    read -p "'$COMPONENT_DIR' already exists. Do you want to remove it? (y/N): " user_input
    if [[ "${user_input}" != "y" ]]; then
        exit 0
    fi
    rm -rf "$COMPONENT_DIR"
fi

if [[ ! -f $SIMPLE_LANGUAGE_JAR ]]; then
    echo "Could not find '$SIMPLE_LANGUAGE_JAR'. Did you run mvn package?"
    exit 1
fi

mkdir -p "$LANGUAGE_PATH"
cp "$SIMPLE_LANGUAGE_JAR" "$LANGUAGE_PATH"

mkdir -p "$LANGUAGE_PATH/launcher"
cp ../launcher/target/sl-launcher.jar "$LANGUAGE_PATH/launcher/"

mkdir -p "$LANGUAGE_PATH/bin"
cp ../sl $LANGUAGE_PATH/bin/
if [[ $INCLUDE_SLNATIVE = "TRUE" ]]; then
    cp ../native/slnative $LANGUAGE_PATH/bin/
fi

mkdir -p "$COMPONENT_DIR/META-INF"
MANIFEST="$COMPONENT_DIR/META-INF/MANIFEST.MF"
touch "$MANIFEST"
echo "Bundle-Name: Simple Language" >> "$MANIFEST"
echo "Bundle-Symbolic-Name: com.oracle.truffle.sl" >> "$MANIFEST"
echo "Bundle-Version: 1.0.0-rc14" >> "$MANIFEST"
echo 'Bundle-RequireCapability: org.graalvm; filter:="(&(graalvm_version=1.0.0-rc14)(os_arch=amd64))"' >> "$MANIFEST"
echo "x-GraalVM-Polyglot-Part: True" >> "$MANIFEST"

pushd "$COMPONENT_DIR" > /dev/null
jar cfm ../sl-component.jar META-INF/MANIFEST.MF .

echo "bin/sl = ../jre/languages/sl/bin/sl" > META-INF/symlinks
if [[ $INCLUDE_SLNATIVE = "TRUE" ]]; then
    echo "bin/slnative = ../jre/languages/sl/bin/slnative" >> META-INF/symlinks
fi
jar uf ../sl-component.jar META-INF/symlinks

echo "jre/languages/sl/bin/sl = rwxrwxr-x" > META-INF/permissions
echo "jre/languages/sl/bin/slnative = rwxrwxr-x" >> META-INF/permissions
jar uf ../sl-component.jar META-INF/permissions
popd > /dev/null
rm -rf "$COMPONENT_DIR"

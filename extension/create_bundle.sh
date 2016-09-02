#!/usr/bin/env bash
# creates the extension bundle
# TODO: define manifest here and pack it

echo "Create extension bundles"
NAME="org.apache.xerces"

jar -cfm ${NAME}.extension-0.0.1.jar ${NAME}/manifest.mf
mv ${NAME}.extension-0.0.1.jar ../src/main/resources/extension/
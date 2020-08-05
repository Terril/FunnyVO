#!/bin/sh

set -e
set -x

ENV_CONFIG=$1
BUILD_NO=$2
APP_VERSION=$3
MODE=$4

if [ "$GLOBAL_ROOT" != "" ]; then
	echo "Doing Global +  config"
else
	if [ "$ENV_CONFIG" != "" ]; then
		echo "Doing Global +  $ENV_CONFIG"
		mystring="$ENV_CONFIG"
		IFS='/' read -a myarray <<< "$mystring"

		echo "${myarray[0]}"

		GLOBAL_ROOT="${myarray[0]}"
		ENVIRONMENT="${myarray[1]}"

		echo "global root $GLOBAL_ROOT"
	fi
fi

cp -rf "config/"$ENV_CONFIG"/google-services.json" "android/app/google-services.json"
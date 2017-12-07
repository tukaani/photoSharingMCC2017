#!/bin/bash

# Please be sure, that 'adb' command from Android SDK is in your PATH variable

# exit if errors
set -e

# Some colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color


# ---- build Android package ----
echo -e "${GREEN}Preparing Android package...${NC}"
cd frontend/PhotoOrganizer
#chmod +x frontend/PhotoOrganizer/gradlew
./gradlew assembleDebug

cd ..
cd ..
echo -e "${NC}The APK file can be found at ./app/build/outputs/apk/depug/app-depug.apk${NC}"

# ---- Deploying cloud functions ----
echo -e "${GREEN}---- Deploying cloud functions ----${GREEN}"
cd firebase-functions
firebase login
firebase deploy
cd ..

# ---- Deploying app  ----
echo -e "${GREEN}---- Deploying app  ---- ${GREEN}"
cd backend
gcloud config set project mcc-fall-2017-g12
gcloud app deploy

#---- Deploying cronjobs  ----
echo -e "${GREEN}---- Deploying cronjobs  ----${GREEN}"
gcloud app deploy cron.yaml

echo -e "${GREEN}---- All done!  ----${GREEN}"
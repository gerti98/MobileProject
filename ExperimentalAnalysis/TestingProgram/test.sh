#!/bin/bash

# 1. Change branch to 'testing_branch' 
# 2. Link your phone with Wireless debugging and run the app
# 3. Login from the app with any account
# 4. Run the script  with > bash test.sh
# 5.wait patiently


START_IN=1
END_IN=80
echo "Countdown"

cd $HOME/Android/Sdk/platform-tools

# ---------------- For testing purposes -------------------

./adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
timeout 15
./adb shell input keyevent 4

./adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
timeout 15
./adb shell input keyevent 4

./adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
timeout 15
./adb shell input keyevent 4

./adb bugreport results/to_be_deleted.zip


# ---------------- Experiments -------------------


./adb shell dumpsys batterystats --reset
for (( c_=$START_IN; c_<=$END_IN; c_++ ))
do
    ./adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
    sleep 15
    ./adb shell input keyevent 4
done

echo "Finished test ONLY MESSAGES"
./adb bugreport results/bugreport_auto_test_only_messages.zip

./adb shell dumpsys batterystats --reset
for (( c_=$START_IN; c_<=$END_IN; c_++ ))
do
    ./adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "MHZsUPUgc0OrK8JDpzts1N27neA3"
    sleep 15
    ./adb shell input keyevent 4
done

echo "Finished test ONE AUDIO"
./adb bugreport results/bugreport_auto_test_one_audio.zip

./adb shell dumpsys batterystats --reset
for (( c_=$START_IN; c_<=$END_IN; c_++ ))
do
    ./adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "p7nq5IXiNGXJ4aSOoD34E2iuKkk1"
    sleep 15
    ./adb shell input keyevent 4
done

echo "Finished test THREE AUDIO"
./adb bugreport results/bugreport_auto_test_three_messages.zip

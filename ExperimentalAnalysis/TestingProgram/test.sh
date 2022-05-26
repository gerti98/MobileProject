#!/bin/bash

# Run with > bash test.sh

START_OUT=1
END_OUT=1
START_IN=1
END_IN=3
echo "Countdown"
 
for (( c=$START_OUT; c<=$END_OUT; c++ ))
do
    # adb shell dumpsys batterystats --reset
    for (( c_=$START_IN; c_<=$END_IN; c_++ ))
    do
        # adb shell am start -n com.example.chatapp/.activity.ChatActivity --es "chat_user_uid" "AR47NLFVJygxpw5is5ZNP3AJZQ03"
        # sleep 10
        # adb shell input keyevent 4
        # echo "$c_"
    done

    echo "Finished test #$c"
    # adb bugreport results/bugreport_auto_test_only_messages_$c.zip
done

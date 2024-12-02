const admin  = require('firebase-admin');
const functions = require('firebase-functions');

var serviceAccount = require("./serviceKey.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://urvoice-98254-default-rtdb.asia-southeast1.firebasedatabase.app"
});

exports.sendNotiToDevices = functions.firestore.document('notifications/{notificationId}')
    .onCreate(async (snap, context) => {
        console.log("New notification created");
        const data = snap.data();

        const notiUser = data.targetUserID;
        const type = data.typeNotification;
        const message = data.message; 

        try {
            // Fetch user tokens from userTokens collection
            const userTokensSnapshot = await admin.firestore().collection('userTokens').doc(notiUser).get();
            if (!userTokensSnapshot.exists) {
                console.log("No user tokens found for user:", notiUser);
                return;
            }

            const userTokens = userTokensSnapshot.data().token;
            console.log("User tokens: ", userTokens);

            if (!Array.isArray(userTokens)) {
                console.error("User tokens is not an array:", userTokens);
                return;
            }

            // Prepare the notification payload
            const payload = {
                notification: {
                    title: "New Notification",
                    body: message,
                },
                data: {
                    click_action: 'FLUTTER_NOTIFICATION_CLICK',
                    typeNotification: type,
                    message: message,
                },
            };

            let response;
            if (userTokens.length === 1) {
                // Send notification to a single token
                response = await admin.messaging().send({
                    ...payload,
                    token: userTokens[0],
                });
                console.log(`Notification sent to single token: ${userTokens[0]}`, response);
            } else {
                // Send notification to multiple tokens
                response = await admin.messaging().sendMulticast({
                    ...payload,
                    tokens: userTokens,
                });

                // Log and filter invalid tokens
                const invalidTokens = [];
                response.responses.forEach((res, idx) => {
                    if (!res.success) {
                        const errorCode = res.error.code;
                        console.error(`Error sending to token ${userTokens[idx]}: ${errorCode}`);
                        invalidTokens.push(userTokens[idx]);
                    }
                });

                console.log("Invalid tokens:", invalidTokens);
                console.log("Notification sent to multiple tokens", response);
            }

            return response;
        } catch (error) {
            console.error("Error sending notification: ", error);
            return error;
        }
    });




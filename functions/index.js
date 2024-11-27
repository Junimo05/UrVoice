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

            let response;
            if (userTokens.length === 1) {
                // Use send method for a single token
                const notimessage = {
                    notification: { title: "New Notification", body: message },
                    token: userTokens[0],
                    data: { 
                      click_action: 'FLUTTER_NOTIFICATION_CLICK',
                      typeNotification: type,
                      message: message 
                    }
                };
                response = await admin.messaging().send(notimessage);
            } else {
                // Use sendMulticast method for multiple tokens
                const notimessage = {
                    notification: { title: "New Notification", body: message },
                    tokens: userTokens,
                    data: { 
                      click_action: 'FLUTTER_NOTIFICATION_CLICK',
                      typeNotification: type,
                      message: message 
                    }
                };
                response = await admin.messaging().sendMulticast(notimessage);
            }

            console.log("Notification sent successfully", response);
            return response;
        } catch (error) {
            console.error("Error sending notification: ", error);
            return error;
        }
    });



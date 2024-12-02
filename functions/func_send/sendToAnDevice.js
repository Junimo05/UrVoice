const admin = require('firebase-admin');

module.exports = async function sendNotiToDevice(deviceToken, title, body) {
    const message = {
      notification: { title: title, body: body },
      token: deviceToken,
      data: { click_action: 'FLUTTER_NOTIFICATION_CLICK' }
    };
    
    try {
      const response = await admin.messaging().send(message);
      console.log("Notification sent successfully", response);
    } catch (error) {
      console.error("Error sending notification: ", error);
    }
};
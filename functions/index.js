//Utils
const {getAccessToken} = require("./Token/token");

// The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
const {logger} = require("firebase-functions");
const {onRequest} = require("firebase-functions/v2/https");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {getDatabase} = require("firebase-admin/database");
const functions = require("firebase-functions");
// The Firebase Admin SDK to access Firestore.
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");

var admin = require("firebase-admin");

var serviceAccount = require("../serviceAccountKey/serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://urvoice-98254-default-rtdb.asia-southeast1.firebasedatabase.app"
});


const FieldValue = require("firebase-admin").firestore.FieldValue;
// Imports the Google Cloud client library
const { PubSub } = require('@google-cloud/pubsub');
// Creates a client; cache this for further use
const pubSubClient = new PubSub();

  /*
    Enable App Check
  */
  //[START enable_app_check]

  //[End Enable App Check]

  /*
  rememer to add 
    headers: {
      'Authorization': 'Bearer ' + accessToken
    }
    to the request headers
  */


  exports.helloWorld_v2 = onRequest(async (request, response) => {
    try {
      const accessToken = await getAccessToken();
      console.log(accessToken);
      response.send("Hello from Firebase!");
    } catch (error) {
      console.error(error);
      response.status(500).send("Error getting access token");
    }
  });

  exports.helloWorld_v1 = functions.region('asia-east2').https.onRequest(async (request, response) => {
    try {
      const accessToken = await getAccessToken();
      console.log(accessToken);
      response.send("Hello from Firebase!");
    } catch (error) {
      console.error(error);
      response.status(500).send("Error getting access token");
    }
  });

// Take the text parameter passed to this HTTP endpoint and insert it into
// Firestore under the path /messages/:documentId/original
exports.addmessage = onRequest(async (req, res) => {
  // Grab the text parameter.
  const original = req.query.text;
  // Push the new message into Firestore using the Firebase Admin SDK.
  const writeResult = await getFirestore()
      .collection("messages")
      .add({original: original});
  // Send back a message that we've successfully written the message
  res.json({result: `Message with ID: ${writeResult.id} added.`});
});

exports.addmessage2 = onCall((request) => {
  if (!request.auth) {
    throw new functions.https.HttpsError(
      'failed-precondition',
      'The function must be called while authenticated.'
    );
  }
  // Grab the text parameter.
  // Message text passed from the client.
  const text = request.data.text;
  // Authentication / user information is automatically added to the request.
  const uid = request.auth.uid;
  const name = request.auth.token.name || null;
  const picture = request.auth.token.picture || null;
  const email = request.auth.token.email || null;
  // Push the new message into Firestore using the Firebase Admin SDK.
  // Saving the new message to the Realtime Database.

  return getDatabase().ref("/messages").push({
    text: text,
    author: {uid, name, picture, email},
  }).then(() => {
    logger.info("New Message written");
    // Returning the sanitized message to the client.
    return {text: sanitizedMessage};
  })
});

// Listens for new messages added to /messages/:documentId/original
// and saves an uppercased version of the message
// to /messages/:documentId/uppercase

exports.makeuppercase = onDocumentCreated("/messages/{documentId}", (event) => {
  // Grab the current value of what was written to Firestore.
  const original = event.data.data().original;

  // Access the parameter `{documentId}` with `event.params`
  logger.log("Uppercasing", event.params.documentId, original);

  const uppercase = original.toUpperCase();

  // You must return a Promise when performing
  // asynchronous tasks inside a function
  // such as writing to Firestore.
  // Setting an 'uppercase' field in Firestore document returns a Promise.
  return event.data.ref.set({uppercase}, {merge: true});
});

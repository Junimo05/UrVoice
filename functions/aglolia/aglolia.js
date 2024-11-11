const functions = require("firebase-functions");
const admin = require("firebase-admin");
const algoliasearch = require('algoliasearch');

admin.initializeApp();

const client = algoliasearch(process.env.AGLOLIA_APP_ID, process.env.AGLOLIA_ADMIN_API_KEY);
const index = client.initIndex(ALGOLIA_INDEX_NAME);



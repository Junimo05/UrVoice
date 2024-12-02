const SCOPES = "https://www.googleapis.com/auth/firebase.messaging";
const { JWT } = require("google-auth-library");

function getAccessToken() {
  return new Promise((resolve, reject) => {
    const key = require("../serviceKey.json");
    const jwtClient = new JWT(
      key.client_email,
      null,
      key.private_key,
      SCOPES,
      null
    );
    jwtClient.authorize((err, tokens) => {
      if (err) {
        reject(err);
        return;
      }
      resolve(tokens.access_token);
    });
  });
}

module.exports = {
  getAccessToken: getAccessToken
};
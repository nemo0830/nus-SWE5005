import axios from "axios";
import forge from "node-forge";
class APIProvider {
  #http = null;
  #serverkey = null;
  #clientKey = null;

  constructor({ url }) {
    this.#http = axios.create({
      baseURL: url,
      headers: { "Content-Type": "application/json" },
    });

    this.getServerPublicKey();
    this.getClientPrivateKey();
  }

  async getClientPrivateKey() {
    const data = await fetch("static/frontEndPrivateKey", {
      method: "GET",
    });
    const keyData = await data.text();
    this.#clientKey = keyData;
    console.log("Client private key downloaded: ", this.#clientKey);
  }

  async getServerPublicKey() {
    const data = await fetch("static/matthew_public_key.pem", {
      method: "GET",
    });
    const pem = await data.text();
    // const pemHeader = "-----BEGIN PUBLIC KEY-----";
    // const pemFooter = "-----END PUBLIC KEY-----";
    // const pemContents = pem.substring(
    //   pemHeader.length,
    //   pem.length - pemFooter.length
    // );

    //let keyArray = await new Blob([pemContents]).arrayBuffer();
    // let reader = new FileReader();
    // await reader.readAsArrayBuffer(blobData);
    this.#serverkey = pem;
    console.log("Server public key downloaded: ", this.#serverkey);
  }

  async encryptData(data) {
    const publicKey = forge.pki.publicKeyFromPem(this.#serverkey);
    const encrypted = publicKey.encrypt(data, "RSA-OAEP", {
      md: forge.md.sha256.create(),
      mgf1: { md: forge.md.sha1.create() },
    });

    console.log("publicKey is ", publicKey);
    console.log("data is ", data);
    console.log("encrypted string", forge.util.encode64(encrypted));

    return encrypted;
  }

  setAuthToken(token) {
    this.#http.defaults.headers.common.Authorization = `Bearer ${token}`;
  }

  clearAuthToken() {
    this.#http.defaults.headers.common.Authorization = "";
  }

  get(resource, query) {
    return this.#http.get(resource, {
      params: query,
    });
  }

  post(resource, data, query) {
    return this.#http.post(resource, data, {
      params: query,
    });
  }

  async submitOrder({ side, ticker, amount, price, userId }) {
    const payload = this.encryptData(
      `${side}#${ticker}#${amount}#${price}#${userId}`
    );
    return this.#http.post(
      `${process.env.VUE_APP_ENDPOINT_ORDERS}/ordermatching/order`,
      payload,
      {
        headers: {
          "Content-Type": "text/plain",
          "aes-key": forge.util.encode64(this.#clientKey),
          "encrypted-message-digest": "text/plain",
        },
      }
    );
  }
}

export default new APIProvider({
  url: process.env.VUE_APP_ENDPOINT_ORDERS,
});

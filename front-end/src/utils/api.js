import axios from "axios";
import forge from "node-forge";
class APIProvider {
  #http = null;
  #serverkey = null;
  #clientKey = null;
  #aesKey = null;

  constructor({ url }) {
    this.#http = axios.create({
      baseURL: url,
      headers: { "Content-Type": "application/json" },
    });

    this.getServerPublicKey();
    this.getClientPrivateKey();
    this.generateAesKey();
  }

  async generateAesKey() {
    const bufferSize = 64;
    const key = forge.random.getBytesSync(32);
    const iv = forge.random.getBytesSync(32);
    const cipher = forge.cipher.createCipher("AES-CBC", key);
    cipher.start({ iv: iv });
    cipher.update(forge.util.createBuffer(bufferSize));
    cipher.finish();
    const encrypted = cipher.output;

    this.#aesKey = forge.util.encode64(encrypted.data);
    console.log("AES key generated\n", this.#aesKey);
  }

  async getClientPrivateKey() {
    const data = await fetch("static/frontEndPrivateKey.pem", {
      method: "GET",
    });
    const keyData = await data.text();
    this.#clientKey = keyData;
    console.log("Client private key downloaded\n", this.#clientKey);
  }

  async getServerPublicKey() {
    const data = await fetch("static/publicKey.pem", {
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
    console.log("Server public key downloaded\n", this.#serverkey);
  }

  signDataWithPrivateKey(data) {
    const hashedData = this.hashData(data);
    const privateKey = forge.pki.privateKeyFromPem(this.#clientKey);
    const pss = forge.pss.create({
      md: forge.md.sha1.create(),
      mgf: forge.mgf.mgf1.create(forge.md.sha1.create()),
      saltLength: 20,
      // optionally pass 'prng' with a custom PRNG implementation
      // optionalls pass 'salt' with a forge.util.ByteBuffer w/custom salt
    });
    const signed = privateKey.sign(hashedData, pss);

    // console.log("privateKey", privateKey);
    // console.log("data", data);
    // console.log("signed string", forge.util.encode64(signed));

    return forge.util.encode64(signed);
  }

  encryptDataWithPublicKey(data) {
    const publicKey = forge.pki.publicKeyFromPem(this.#serverkey);
    const encrypted = publicKey.encrypt(data, "RSA-OAEP", {
      md: forge.md.sha256.create(),
      mgf1: { md: forge.md.sha1.create() },
    });

    // console.log("publicKey", publicKey);
    // console.log("data", data);
    // console.log("encrypted string", forge.util.encode64(encrypted));

    return forge.util.encode64(encrypted);
  }

  hashData(data) {
    const md = forge.md.sha256.create();
    md.update(data);
    //const hash = md.digest().toHex();

    //console.log("hash", hash);

    return md;
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
    return this.#http.post(
      `${process.env.VUE_APP_ENDPOINT_ORDERS}/ordermatching/order`,
      `${side}#${ticker}#${amount}#${price}#${userId}`,
      {
        headers: { "Content-Type": "text/plain" },
      }
    );
  }

  async submitOrderSecure({ side, ticker, amount, price, userId }) {
    const payload = this.hashData(
      `${side}#${ticker}#${amount}#${price}#${userId}`
    );
    const hashedPayload = this.signDataWithPrivateKey(payload);
    const headers = {
      headers: {
        "Content-Type": "text/plain",
        "aes-key": this.encryptDataWithPublicKey(this.#aesKey),
        "encrypted-message-digest": hashedPayload,
      },
    };

    console.log("[submitOrder] payload\n", hashedPayload);
    console.log("[submitOrder] headers\n", headers);

    return this.#http.post(
      `${process.env.VUE_APP_ENDPOINT_ORDERS}/ordermatching/order`,
      hashedPayload,
      headers
    );
  }
}

export default new APIProvider({
  url: process.env.VUE_APP_ENDPOINT_ORDERS,
});

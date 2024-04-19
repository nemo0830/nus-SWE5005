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

    this.#aesKey = "1234567890_1234567890_1234567890";

    this.getServerPublicKey();
    this.getClientPrivateKey();
    // this.generateAesKey();
  }

  encryptDataWithAesKey(data) {
    //const bufferSize = 64;
    try {
      // const key = forge.random.getBytesSync(32);
      const iv = forge.random.getBytesSync(32);
      const cipher = forge.cipher.createCipher("AES-ECB", this.#aesKey);

      cipher.start({ iv: iv });
      cipher.update(forge.util.createBuffer(data));
      cipher.finish();

      console.log("[encryptDataWithAesKey] Input data", data);
      // const encrypted = cipher.output;
      const encoded = forge.util.encode64(cipher.output.data);
      // console.log("[encryptDataWithAesKey] Payload", cipher.output);
      console.log("[encryptDataWithAesKey] Encrypted payload", encoded);

      return encoded;
    } catch (e) {
      console.error(e);
    }
    return null;
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
    const privateKey = forge.pki.privateKeyFromPem(this.#clientKey);
    // const pss = forge.pss.create({
    //   md: forge.md.sha1.create(),
    //   mgf: forge.mgf.mgf1.create(forge.md.sha1.create()),
    //   saltLength: 20,
    // optionally pass 'prng' with a custom PRNG implementation
    // optionalls pass 'salt' with a forge.util.ByteBuffer w/custom salt
    // });
    const md = forge.md.sha256.create();
    md.update(data);
    const signed = privateKey.sign(md);

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
    const hash = md.digest().getBytes();

    console.log("[hashData] encoded data is ", forge.util.encode64(hash));

    return forge.util.encode64(hash);
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
    const payload = `${side}#${ticker}#${amount}#${price}#${userId}`;
    const hashedPayload = this.hashData(payload);
    const encryptedPayload = this.encryptDataWithAesKey(payload);
    const encryptionKey = this.encryptDataWithPublicKey(
      forge.util.encode64(this.#aesKey)
    );

    console.log("hashedPayload is ", hashedPayload);
    //console.log("signedPayload is ", signedPayload);

    const headers = {
      headers: {
        "Content-Type": "text/plain",
        "aes-key": encryptionKey,
        "encrypted-message-digest": this.signDataWithPrivateKey(payload),
      },
    };

    console.log("[submitOrderSecure] headers\n", headers);

    return this.#http.post(
      `${process.env.VUE_APP_ENDPOINT_ORDERS}/ordermatching/secure/order`,
      encryptedPayload,
      headers
    );
  }
}

export default new APIProvider({
  url: process.env.VUE_APP_ENDPOINT_ORDERS,
});

import axios from "axios";

class APIProvider {
  #http = null;

  constructor({ url }) {
    this.#http = axios.create({
      baseURL: url,
      headers: { "Content-Type": "application/json" },
    });
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

  submitOrder({ side, ticker, amount, price, userId }) {
    return this.#http.post(
      `${process.env.VUE_APP_ENDPOINT_ORDERS}/ordermatching/order`,
      `${side}#${ticker}#${amount}#${price}#${userId}`,
      {
        headers: { "Content-Type": "text/plain" },
      }
    );
  }
}

export default new APIProvider({
  url: process.env.VUE_APP_ENDPOINT_ORDERS,
});

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

  // REST Methods
  get(resource, query) {
    console.log("resource is " + resource);
    return this.#http.get(resource, {
      params: query,
    });
  }

  getObject(resource, id, query) {
    return this.#http.get(`${resource}/${id}`, {
      params: query,
    });
  }

  create(resource, data, query) {
    return this.#http.post(resource, data, {
      params: query,
    });
  }

  update(resource, id, data, query) {
    return this.#http.patch(`${resource}/${id}`, data, {
      params: query,
    });
  }

  destroy(resource, id) {
    return this.#http.delete(`${resource}/${id}`);
  }
}

export default new APIProvider({
  url: process.env.VUE_APP_ENDPOINT_ORDERS,
});

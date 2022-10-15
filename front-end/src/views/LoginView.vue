<template>
  <v-container fill-height fluid>
    <v-row align="center" justify="center">
      <v-col>
        <v-card class="mx-auto" max-width="344">
          <v-card-actions>
            <v-col class="text-center">
              <v-btn color="cyan accent-4" dark @click="signUp"
                >Login / Sign Up</v-btn
              >
            </v-col>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import axios from "axios";
import { mapMutations } from "vuex";
import Keycloak from "keycloak-js";

export default {
  name: "LoginView",
  components: {
    // HelloWorld,
  },
  data() {
    return {
      username: "",
      password: "",
    };
  },
  mounted() {},
  methods: {
    ...mapMutations({
      saveUserData: "saveUserState",
    }),
    async login() {
      console.log(`LOGIN with ${this.username}, ${this.password}`);
      let result = await axios.post(
        `${process.env.VUE_APP_ENDPOINT_ACCOUNTS}/account/userLogon`,
        `${this.username}#${this.password}`,
        {
          headers: {
            "Content-Type": "text/plain",
          },
        }
      );
      if (result.status === 200 && result.data.success === true) {
        console.log("LOGIN SUCCESS");
        this.saveUserData(result.data.data);
        this.$router.push("/dashboard");
      } else {
        console.log("LOGIN FAILED");
      }
    },
    signUp() {
      let initOptions = {
        url: "https://oauth.omni-trade.xyz/",
        realm: "IamOrderMatching",
        clientId: "iam-ordermatching",
        onLoad: "login-required",
      };

      let keycloak = Keycloak(initOptions);

      keycloak
        .init({
          onLoad: initOptions.onLoad,
          redirectUri: "http://localhost:8888",
        })
        .then((auth) => {
          if (!auth) {
            window.location.reload();
            console.log.info("error");
          } else {
            console.log.info("Authenticated");
            console.log.info("keycloak.token:" + keycloak.token);
            console.log.info("keycloak.refreshToken" + keycloak.refreshToken);
            localStorage.setItem("vue-token", keycloak.token);
            localStorage.setItem("vue-refresh-token", keycloak.refreshToken);
          }

          //Token Refresh
          setInterval(() => {
            keycloak
              .updateToken(70)
              .then((refreshed) => {
                if (refreshed) {
                  console.log.info("Token refreshed" + refreshed);
                } else {
                  console.log.warn(
                    "Token not refreshed, valid for " +
                      Math.round(
                        keycloak.tokenParsed.exp +
                          keycloak.timeSkew -
                          new Date().getTime() / 1000
                      ) +
                      " seconds"
                  );
                }
              })
              .catch(() => {
                console.log.error("Failed to refresh token");
              });
          }, 6000);
        })
        .catch(() => {
          console.log.error("Authenticated Failed");
        });
    },
  },
};
</script>

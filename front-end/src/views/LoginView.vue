<template>
  <v-container fill-height fluid>
    <v-row align="center" justify="center">
      <v-col>
        <v-card class="mx-auto" max-width="344">
          <v-card-actions>
            <v-col class="text-center">
              <template v-if="email != null">
                <h3>Hello, {{ email }}</h3>
              </template>
              <template v-else>
                <h3>Please sign in</h3>
              </template>
              <br />
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
      email: null,
      keycloak: null,
    };
  },
  mounted() {
    let initOptions = {
      url: `${process.env.VUE_APP_ENDPOINT_OAUTH}`,
      realm: "IamOrderMatching",
      clientId: "iam-ordermatching",
      onLoad: "check-sso",
    };

    this.keycloak = new Keycloak(initOptions);

    this.keycloak
      .init({
        onLoad: initOptions.onLoad,
        // redirectUri: `${process.env.VUE_APP_ENDPOINT_OAUTH_REDIRECT_URI}`,
      })
      .then((auth) => {
        if (!auth) {
          console.info("error");
        } else {
          console.info("Authenticated");
          console.info(this.keycloak.tokenParsed);
          console.info("keycloak.token:" + this.keycloak.token);
          console.info("keycloak.refreshToken" + this.keycloak.refreshToken);
          // localStorage.setItem("vue-token", this.keycloak.token);
          // localStorage.setItem("vue-refresh-token", this.keycloak.refreshToken);
          this.email = this.keycloak.tokenParsed.email;
        }

        //Token Refresh
        setInterval(() => {
          this.keycloak
            .updateToken(70)
            .then((refreshed) => {
              if (refreshed) {
                console.info("Token refreshed" + refreshed);
              } else {
                console.warn(
                  "Token not refreshed, valid for " +
                    Math.round(
                      this.keycloak.tokenParsed.exp +
                        this.keycloak.timeSkew -
                        new Date().getTime() / 1000
                    ) +
                    " seconds"
                );
              }
            })
            .catch(() => {
              console.error("Failed to refresh token");
            });
        }, 6000);
      })
      .catch(() => {
        console.error("Authenticated Failed");
      });
  },
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
      this.keycloak.login();
    },
  },
};
</script>

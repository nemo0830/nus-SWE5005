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
              <v-btn
                color="cyan accent-4"
                dark
                @click="signUp"
                v-if="!isLoggedIn"
                >Login / Sign Up</v-btn
              >
              <br />
              <br />
              <v-btn
                color="cyan accent-4"
                dark
                @click="logOut"
                v-if="isLoggedIn"
                >Log out</v-btn
              >
            </v-col>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { mapMutations } from "vuex";
import Keycloak from "keycloak-js";

export default {
  name: "LoginView",
  components: {},
  computed: {
    isLoggedIn() {
      return this.$store.getters.isLoggedIn;
    },
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
          // console.info("keycloak.token:" + this.keycloak.token);
          // console.info("keycloak.refreshToken" + this.keycloak.refreshToken);
          this.saveUserData({
            token: this.keycloak.token,
            refreshToken: this.keycloak.refreshToken,
            userId: this.keycloak.subject,
          });
          // this.email = this.keycloak.tokenParsed.email;
          this.email = `${this.keycloak.tokenParsed.preferred_username} (${this.keycloak.tokenParsed.email})`;
          this.$api.setAuthToken(this.keycloak.token);
          // this.$router.push("/dashboard");
        }

        //Token Refresh
        setInterval(() => {
          this.keycloak
            .updateToken(70)
            .then((refreshed) => {
              if (refreshed) {
                console.info("Token refreshed" + refreshed);
                this.saveUserData({
                  token: this.keycloak.token,
                  refreshToken: this.keycloak.refreshToken,
                });
                this.$api.setAuthToken(this.keycloak.token);
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
    signUp() {
      this.keycloak.login();
    },
    logOut() {
      this.keycloak.logout();
    },
  },
};
</script>

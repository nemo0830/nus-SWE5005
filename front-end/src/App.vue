<template>
  <v-app>
    <v-main>
      <v-app-bar color="teal lighten-1" dark>
        <v-app-bar-nav-icon @click="drawer = true"></v-app-bar-nav-icon>

        <v-toolbar-title>Omni Trade</v-toolbar-title>
      </v-app-bar>

      <v-navigation-drawer v-model="drawer" absolute temporary>
        <v-list nav dense>
          <v-list-item-group
            v-model="group"
            active-class="teal--text text--accent-4"
            mandatory
          >
            <router-link :to="homePath" class="text-decoration-none">
              <v-list-item>
                <v-list-item-icon>
                  <v-icon>mdi-home</v-icon>
                </v-list-item-icon>
                <v-list-item-title>Home</v-list-item-title>
              </v-list-item>
            </router-link>
            <router-link :to="accountPath" class="text-decoration-none">
              <v-list-item>
                <v-list-item-icon>
                  <v-icon>mdi-account</v-icon>
                </v-list-item-icon>
                <v-list-item-title>Account</v-list-item-title>
              </v-list-item>
            </router-link>
            <!-- <router-link to="/account" class="text-decoration-none">
              <v-list-item>
                <v-list-item-icon>
                  <v-icon>mdi-book-open-variant</v-icon>
                </v-list-item-icon>
                <v-list-item-title>Order Book</v-list-item-title>
              </v-list-item>
            </router-link> -->
            <router-link
              v-if="isLoggedIn"
              to="/dashboard"
              class="text-decoration-none"
            >
              <v-list-item>
                <v-list-item-icon>
                  <v-icon>mdi-book-open-variant</v-icon>
                </v-list-item-icon>
                <v-list-item-title>Buy / Sell</v-list-item-title>
              </v-list-item>
            </router-link>
            <router-link to="/about" class="text-decoration-none">
              <v-list-item>
                <v-list-item-icon>
                  <v-icon>mdi-lifebuoy</v-icon>
                </v-list-item-icon>
                <v-list-item-title>About</v-list-item-title>
              </v-list-item>
            </router-link>
          </v-list-item-group>
        </v-list>
      </v-navigation-drawer>
      <router-view />
    </v-main>
  </v-app>
</template>

<script>
// import { mapGetters } from "vuex";

export default {
  name: "App",
  computed: {
    homePath() {
      if (this.$store.getters.isLoggedIn) {
        return "/";
      } else {
        return "/";
      }
    },
    accountPath() {
      if (this.$store.getters.isLoggedIn) {
        return "/login";
      } else {
        return "/login";
      }
    },
    isLoggedIn() {
      return this.$store.getters.isLoggedIn;
    },
  },
  data: () => ({
    drawer: false,
    group: null,
  }),
};
</script>

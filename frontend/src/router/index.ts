import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SignupView from "@/views/SignupView.vue";
import LoginView from "@/views/LoginView.vue";
import BookDetail from "@/views/BookDetail.vue";
import CreateBookView from "@/components/CreateBookForm.vue";
import {isLoggedIn} from "@/service/useSession";
import EditUserDetails from "@/views/EditUserDetails.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login/oauth2/code/github',
      name: 'OAuthCallback',
      component: () => import('@/views/OAuthCallback.vue')
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/book/:id',
      name: 'book-detail',
      component: BookDetail, // This is where the book detail component will be loaded
      props: true, // Pass the ISBN as a prop to the component
    },
    {
      path: '/create-book',
      name: 'create-book',
      component: CreateBookView,
      beforeEnter: (to, from, next) => {
        if (isLoggedIn.value) {
          next(); // Allow access
        } else {
          next("/login"); // Redirect to login page
        }
      },
    },
    {
      path: '/edit-user',
      name: 'edit-user',
      component: EditUserDetails,
      beforeEnter: (to, from, next) => {
        if (isLoggedIn.value) {
          next(); // Allow access
        } else {
          next('/login'); // Redirect to login page
        }
      },
    },
  ],
})

export default router

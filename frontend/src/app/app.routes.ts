import { Routes } from '@angular/router';
import {Books} from './pages/books/books';
import {BookDetail} from './pages/book-detail/book-detail';
import {AuthForm} from './pages/auth-form/auth-form';
import {ProfileSettings} from './pages/profile-settings/profile-settings';
import {CreateBook} from './pages/create-book/create-book';
import {GithubCallback} from './pages/github-callback/github-callback';

export const routes: Routes = [
  { path: '', redirectTo: '/books', pathMatch: 'full' },
  { path: 'books', component: Books },
  { path: 'books/:id', component: BookDetail },
  { path: 'login', component: AuthForm },
  { path: 'signup', component: AuthForm },
  { path: 'profile', component: ProfileSettings },
  { path: 'create-book', component: CreateBook },
  { path: 'login/oauth2/code/github', component: GithubCallback }
];

import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { NavbarComponent } from './components/navbar-component/navbar-component';
import {ChatWidget} from './components/chat-widget/chat-widget';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ButtonModule, NavbarComponent, ChatWidget],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');
}

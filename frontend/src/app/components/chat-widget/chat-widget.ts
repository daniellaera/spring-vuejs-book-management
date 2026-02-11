import { Component, ViewChild, ElementRef, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { TooltipModule } from 'primeng/tooltip';
import { ChatAiService } from '../../services/chat-ai.service';
import { AuthService } from '../../services/auth.service';
import {ChatMessage} from '../../models/chat-message.model';
import {InputText} from 'primeng/inputtext';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, BadgeModule, TooltipModule, InputText],
  templateUrl: './chat-widget.html',
  styleUrl: './chat-widget.scss'
})
export class ChatWidget {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  isOpen = signal(false);
  isLoading = signal(false);
  userMessage = '';
  messages = signal<ChatMessage[]>([]);

  isLoggedIn = computed(() => this.authService.isLoggedIn());
  userName = computed(() => this.authService.getFullName());

  constructor(
    private chatAiService: ChatAiService,
    private authService: AuthService
  ) {}

  toggleChat(): void {
    this.isOpen.update(v => !v);

    if (this.isOpen() && this.messages().length === 0 && this.isLoggedIn()) {
      this.messages.set([{
        role: 'assistant',
        content: `Hi ${this.userName()}! 👋 I'm your library assistant. Ask me anything about our book collection!`,
        timestamp: new Date()
      }]);
    }

    if (this.isOpen()) {
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  sendMessage(): void {
    const question = this.userMessage.trim();
    if (!question || this.isLoading()) return;

    // Add user message
    this.messages.update(msgs => [...msgs, {
      role: 'user' as const,
      content: question,
      timestamp: new Date()
    }]);

    this.userMessage = '';
    this.isLoading.set(true);

    // Add empty assistant message for streaming
    const assistantMessage: ChatMessage = {
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      isStreaming: true
    };

    this.messages.update(msgs => [...msgs, assistantMessage]);
    this.scrollToBottom();

    this.chatAiService.askStream(question).subscribe({
      next: (chunk) => {
        assistantMessage.content += chunk;
        this.messages.update(msgs => [...msgs]);
        this.scrollToBottom();
      },
      error: (err) => {
        console.error('Stream error:', err);
        assistantMessage.content = 'Sorry, something went wrong. Please try again.';
        assistantMessage.isStreaming = false;
        this.isLoading.set(false);
        this.messages.update(msgs => [...msgs]);
        this.scrollToBottom();
      },
      complete: () => {
        // Format the complete message - add newlines before numbered items
        assistantMessage.content = assistantMessage.content.replace(/(\d+)\.\s/g, '\n$1. ');
        assistantMessage.isStreaming = false;
        this.isLoading.set(false);
        this.messages.update(msgs => [...msgs]);
        this.scrollToBottom();
      }
    });
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  clearChat(): void {
    this.messages.set([{
      role: 'assistant',
      content: `Chat cleared! How can I help you?`,
      timestamp: new Date()
    }]);
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer) {
        const el = this.messagesContainer.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    }, 50);
  }
}

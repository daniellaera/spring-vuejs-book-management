import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatAiService {

  private apiUrl = `${environment.apiUrl}/ai/books`;

  askStream(question: string): Observable<string> {
    return new Observable(observer => {
      const token = localStorage.getItem('auth_token');
      const url = `${this.apiUrl}/ask/stream?question=${encodeURIComponent(question)}`;

      fetch(url, {
        method: 'GET',
        headers: {
          'Accept': 'text/event-stream',
          'Authorization': `Bearer ${token}`
        }
      }).then(response => {
        if (!response.ok) {
          observer.error(`HTTP ${response.status}`);
          return;
        }

        const reader = response.body?.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        if (!reader) {
          observer.error('No reader available');
          return;
        }

        const read = (): void => {
          reader.read().then(({ done, value }) => {
            if (done) {
              // Process any remaining buffer
              if (buffer.trim()) {
                processBuffer();
              }
              observer.complete();
              return;
            }

            buffer += decoder.decode(value, { stream: true });
            processBuffer();
            read();
          }).catch(err => observer.error(err));
        };

        const processBuffer = (): void => {
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          for (const line of lines) {
            if (line.startsWith('data:')) {
              const text = line.substring(5);
              if (text === '') {
                observer.next('\n');  // Empty data line = newline from Claude
              } else {
                observer.next(text);
              }
            }
          }
        };

        read();
      }).catch(err => observer.error(err));
    });
  }
}

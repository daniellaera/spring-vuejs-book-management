#!/bin/sh

if [ -n "$VITE_API_BASE_URL" ]; then
    echo "Injecting VITE_API_BASE_URL into index.html"
    # Replace the placeholder with the actual API URL in the index.html file
    sed -i "s|\${VITE_API_BASE_URL}|$VITE_API_BASE_URL|g" /usr/share/nginx/html/index.html
else
    echo "No VITE_API_BASE_URL provided"
fi

# Start Nginx to serve the application
exec "$@"
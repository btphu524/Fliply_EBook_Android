# Optimized Dockerfile với layer caching
FROM node:18-slim

WORKDIR /app

# Set environment variables
ENV NODE_ENV=production \
    PORT=3000 \
    HOST=0.0.0.0

# Install dependencies trước (tận dụng Docker layer caching)
# Chỉ copy package files để cache layer này nếu dependencies không thay đổi
COPY package*.json ./

# Install production dependencies
RUN npm ci --omit=dev --no-audit --no-fund && \
    npm cache clean --force

# Copy source code (layer này sẽ rebuild khi code thay đổi)
COPY . .

# Tạo user không phải root để tăng bảo mật
RUN groupadd -r nodejs && useradd -r -g nodejs nodejs && \
    chown -R nodejs:nodejs /app

# Switch sang user nodejs
USER nodejs

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD node -e "require('http').get('http://localhost:3000/health', (r) => {process.exit(r.statusCode === 200 ? 0 : 1)})" || exit 1

# Start application
CMD ["npm", "start"]



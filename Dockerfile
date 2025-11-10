# ======================================
# Stage 1: Build the Spring Boot JAR
# ======================================
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# ======================================
# Stage 2: Runtime
# ======================================
FROM eclipse-temurin:17-jdk AS runtime
WORKDIR /app

# --- Set DEBIAN_FRONTEND to noninteractive for all apt commands ---
ENV DEBIAN_FRONTEND=noninteractive

# --- Install system dependencies & pip ---
# Reverting to 'python3-pip' from apt, as it's the standard Debian way.
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    # --- Core packages ---
    python3 \
    python3-pip \
    git \
    wget \
    # --- Dependencies for OpenCV & Pillow ---
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    libgl1 \
    libjpeg-dev \
    zlib1g-dev \
    && \
    # --- Clean up apt cache ---
    rm -rf /var/lib/apt/lists/*

# --- (REMOVED) Upgrade pip ---
# This step was consistently failing, so we are skipping it.
# We will use the pip version from apt-get.

# --- Install PyTorch ---
# Add --break-system-packages here as well.
RUN python3 -m pip install --no-cache-dir torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu --break-system-packages

# --- Install remaining Python packages ---
# And add --break-system-packages here.
RUN python3 -m pip install --no-cache-dir ultralytics==8.3.0 opencv-python pillow --break-system-packages

# --- Copy app ---
COPY --from=builder /app/target/*.jar app.jar
COPY model ./model
COPY src/main/resources/scripts ./scripts

ENV MODEL_PATH=/app/model/best.pt
ENV PYTHONUNBUFFERED=1
ENV SERVER_PORT=8081

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
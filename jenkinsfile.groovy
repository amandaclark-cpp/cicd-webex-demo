pipeline {
  agent any

  options {
    timestamps()
  }

  triggers {
    // So GitHub webhook can trigger builds
    githubPush()
  }

  stages {

    stage('Checkout Code') {
      steps {
        // Uses the same repo/branch the job is configured with 
        checkout scm
      }
    }

    stage('Setup Python venv') {
      steps {
        sh '''
          set -eux

          python3 --version

          # Create virtual environment in workspace if it doesn't exist
          if [ ! -d venv ]; then
            python3 -m venv venv
          fi

          # Activate venv
          . venv/bin/activate

          # Install required tools inside venv
          pip install --upgrade pip
          pip install pytest pycodestyle

          # Make sure scripts are executable
          chmod +x build.sh test.sh
        '''
      }
    }

    stage('Build') {
      steps {
        sh '''
          set -eux
          . venv/bin/activate
          ./build.sh
        '''
      }
    }

    stage('Test') {
      steps {
        sh '''
          set -eux
          . venv/bin/activate
          ./test.sh
        '''
      }
    }
  }

  post {

    success {
      // SIMPLE: hard-coded WebEx bot token + roomId
      // Replace YOUR_BOT_TOKEN_HERE and YOUR_ROOM_ID_HERE with your real values
      sh '''
        curl -sS -X POST "https://webexapis.com/v1/messages" \
          -H "Authorization: Bearer ZmI5MmZjYmUtNDk4NS00MGY4LWE1YzYtOWI0MTQwMWM4ZmEyZDdjNzE1MDEtNjMx_P0A1_13494cac-24b4-4f89-8247-193cc92a7636" \
          -H "Content-Type: application/json" \
          -d @- <<'JSON'
        {
          "roomId": "Y2lzY29zcGFyazovL3VybjpURUFNOnVzLXdlc3QtMl9yL1JPT00vZDk0OTA0OTAtYzAzYi0xMWYwLWFlZTMtNTNmNWQxYTQ5NGI5",
          "text": "✅ Jenkins build SUCCEEDED for cicd-webex-demo."
        }
JSON
      '''
    }

    failure {
      sh '''
        curl -sS -X POST "https://webexapis.com/v1/messages" \
          -H "Authorization: Bearer ZmI5MmZjYmUtNDk4NS00MGY4LWE1YzYtOWI0MTQwMWM4ZmEyZDdjNzE1MDEtNjMx_P0A1_13494cac-24b4-4f89-8247-193cc92a7636" \
          -H "Content-Type: application/json" \
          -d @- <<'JSON'
        {
          "roomId": "YY2lzY29zcGFyazovL3VybjpURUFNOnVzLXdlc3QtMl9yL1JPT00vZDk0OTA0OTAtYzAzYi0xMWYwLWFlZTMtNTNmNWQxYTQ5NGI5",
          "text": "❌ Jenkins build FAILED for cicd-webex-demo."
        }
JSON
      '''
    }
  }
}

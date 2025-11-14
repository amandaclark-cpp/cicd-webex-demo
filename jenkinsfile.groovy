pipeline {
  agent any

  environment {
    WEBEX_TOKEN = credentials('WEBEX_BOT_TOKEN')
    WEBEX_ROOM  = credentials('WEBEX_ROOM_ID')
  }

  options {
    timestamps()
  }

  triggers {
    githubPush()
  }

  stages {
    stage('Checkout Code') {
      steps {
        // Use SCM config from the job
        checkout scm
      }
    }

    stage('System Prep (Python)') {
      steps {
        sh '''
          set -eux
          if ! command -v python3 >/dev/null; then
            apt-get update
            DEBIAN_FRONTEND=noninteractive apt-get install -y python3 python3-pip curl
          fi
          python3 -m pip install --upgrade pip
          pip3 install pytest pycodestyle
          chmod +x build.sh test.sh
        '''
      }
    }

    stage('Build') {
      steps {
        sh './build.sh'
      }
    }

    stage('Test') {
      steps {
        sh './test.sh'
      }
    }
  }

  post {
    success {
      sh '''
        curl -S -X POST "https://webexapis.com/v1/messages" \
          -H "Authorization: Bearer ${WEBEX_TOKEN}" \
          -H "Content-Type: application/json" \
          -d @- <<'JSON'
        {
          "roomId": "${WEBEX_ROOM}",
          "markdown": "**Jenkins Build Succeeded**: ${JOB_NAME} #${BUILD_NUMBER}\\nCommit: ${GIT_COMMIT}\\n<${BUILD_URL}|Open in Jenkins>"
        }
JSON
      '''
    }
    failure {
      sh '''
        curl -S -X POST "https://webexapis.com/v1/messages" \
          -H "Authorization: Bearer ${WEBEX_TOKEN}" \
          -H "Content-Type: application/json" \
          -d @- <<'JSON'
        {
          "roomId": "${WEBEX_ROOM}",
          "markdown": "**Jenkins Build Failed**: ${JOB_NAME} #${BUILD_NUMBER}\\nCommit: ${GIT_COMMIT}\\n<${BUILD_URL}|Open in Jenkins>"
        }
JSON
      '''
    }
  }
}

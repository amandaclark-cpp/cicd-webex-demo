pipeline {
  agent any

  options {
    timestamps()
  }

  triggers {
    githubPush()
  }

  stages {
    stage('Checkout Code') {
      steps {
        checkout scm
      }
    }

  stage('System Prep (Python)') {
    steps {
      sh '''
        set -eux
        python3 --version

        #Create and use a virtual environment in the workspace
        python3 -m venv venv
        
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
      withCredentials([
        string(credentialsId: 'WEBEX_BOT_TOKEN1', variable: 'WEBEX_TOKEN'),
        string(credentialsId: 'WEBEX_ROOM_ID1', variable: 'WEBEX_ROOM')
      ]) {
        sh '''
          curl -sS -X POST "https://webexapis.com/v1/messages" \
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
    }
    failure {
      withCredentials([
        string(credentialsId: 'WEBEX_BOT_TOKEN', variable: 'WEBEX_TOKEN'),
        string(credentialsId: 'WEBEX_ROOM_ID', variable: 'WEBEX_ROOM')
      ]) {
        sh '''
          curl -sS -X POST "https://webexapis.com/v1/messages" \
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
}


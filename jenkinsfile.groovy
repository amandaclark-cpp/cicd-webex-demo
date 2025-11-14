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
      //Temp: hard-code values for this assignment
        sh '''
          curl -sS -X POST "https://webexapis.com/v1/messages" \
            -H "Authorization: Bearer ZmI5MmZjYmUtNDk4NS00MGY4LWE1YzYtOWI0MTQwMWM4ZmEyZDdjNzE1MDEtNjMx_P0A1_13494cac-24b4-4f89-8247-193cc92a7636" \
            -H "Content-Type: application/json" \
            -d @- <<'JSON'
          {
            "roomId": "Y2lzY29zcGFyazovL3VybjpURUFNOnVzLXdlc3QtMl9yL1JPT00vZDk0OTA0OTAtYzAzYi0xMWYwLWFlZTMtNTNmNWQxYTQ5NGI5",
            "markdown": "**Jenkins Build Succeeded**: ${JOB_NAME} #${BUILD_NUMBER}\\nCommit: ${GIT_COMMIT}\\n<${BUILD_URL}|Open in Jenkins>"
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
            "roomId": "Y2lzY29zcGFyazovL3VybjpURUFNOnVzLXdlc3QtMl9yL1JPT00vZDk0OTA0OTAtYzAzYi0xMWYwLWFlZTMtNTNmNWQxYTQ5NGI5",
            "markdown": "**Jenkins Build Failed**: ${JOB_NAME} #${BUILD_NUMBER}\\nCommit: ${GIT_COMMIT}\\n<${BUILD_URL}|Open in Jenkins>"
          }
JSON
        '''
      }
    }
}

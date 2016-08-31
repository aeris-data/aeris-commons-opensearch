define({ "api": [
  {
    "type": "get",
    "url": "/calendar/check",
    "title": "Vérifier la disponibilité des quicklooks pour une période donnée",
    "group": "Calendar",
    "name": "CheckAvailability",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "collection",
            "description": "<p>Collection des quicklooks</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "start",
            "description": "<p>Date de début (YYYY-MM-DD)</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "end",
            "description": "<p>Date de fin (YYYY-MM-DD)</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Example-Usage:",
        "content": "http://yourserver.com/calendar/check?collection=collectionName&start=YYYY-MM-DD&end=YYYY-MM-DD",
        "type": "json"
      }
    ],
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Array",
            "optional": false,
            "field": "events",
            "description": "<p>Liste des disponibilités</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK\n{\n  \"events\": [{\n      \"is\": \"full\",\n      \"start\": \"YYYY-MM-DD\",\n      \"end\": \"YYYY-MM-DD\",\n      \"comment\": \"\",\n      \"color\": \"green\"\n    }]\n}",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "BadRequest",
            "description": "<p>La requête est mal formulée</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/CalendarService.java",
    "groupTitle": "Calendar"
  },
  {
    "type": "get",
    "url": "/files/get",
    "title": "Télécharger un fichier",
    "group": "Files",
    "name": "getFile",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "path",
            "description": "<p>Chemin du fichier à télécharger</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "File",
            "optional": false,
            "field": "Fichier",
            "description": "<p>Fichier demandé</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Error": [
          {
            "group": "Error",
            "optional": false,
            "field": "204_NoContent",
            "description": "<p>Aucun fichier trouvé</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/FileService.java",
    "groupTitle": "Files"
  },
  {
    "type": "get",
    "url": "/files/list",
    "title": "Lister les fichiers contenus dans un dossier",
    "group": "Files",
    "name": "listFiles",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "path",
            "description": "<p>Chemin du dossier à analyser</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Array",
            "optional": false,
            "field": "Response",
            "description": "<p>Description de l'ensemble des fichiers et dossiers disponibles</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": " HTTP/1.1 200 OK\n [\n\t\t  {\n\t\t    \"type\": \"folder\",\n\t\t    \"name\": \"folderName\",\n\t\t    \"extension\": \"\",\n\t\t    \"path\": \"folderPathFromRoot\",\n\t\t    \"url\": \"\",\n\t\t    \"size\": folderSize,\n\t\t    \"modified\": \"YYYY-MM-DD HH:mm\"\n\t\t  },\n\t\t  {\n\t\t    \"type\": \"file\",\n\t\t    \"name\": \"fileName\",\n\t\t    \"extension\": \"fileExtension\",\n\t\t    \"path\": \"filePathFromRoot\",\n\t\t    \"url\": \"http://yourserver.com/files/get?path=filePath\",\n\t\t    \"size\": fileSize,\n\t\t    \"modified\": \"YYYY_MM-DD HH:mm\"\n\t\t  }\n\t\t]",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error": [
          {
            "group": "Error",
            "optional": false,
            "field": "204_NoContent",
            "description": "<p>Aucun fichier ou dossier à afficher</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/FileService.java",
    "groupTitle": "Files"
  },
  {
    "type": "get",
    "url": "/files/getimage",
    "title": "Récupérer une image",
    "group": "Files",
    "name": "serveImage",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "q",
            "description": "<p>Chemin de l'image</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "File",
            "optional": false,
            "field": "Image",
            "description": "<p>Fichier demandé</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Error": [
          {
            "group": "Error",
            "optional": false,
            "field": "204_NoContent",
            "description": "<p>Aucun fichier trouvé</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/FileService.java",
    "groupTitle": "Files"
  },
  {
    "type": "get",
    "url": "/files/view",
    "title": "Récupérer un fichier visualisable (pdf / html)",
    "group": "Files",
    "name": "showFile",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "path",
            "description": "<p>Chemin du fichier</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "File",
            "optional": false,
            "field": "Fichier",
            "description": "<p>Fichier demandé</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Error": [
          {
            "group": "Error",
            "optional": false,
            "field": "204_NoContent",
            "description": "<p>Aucun fichier trouvé</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/FileService.java",
    "groupTitle": "Files"
  },
  {
    "type": "post",
    "url": "/files/list",
    "title": "Envoyer des fichiers dans un dossier",
    "group": "Files",
    "name": "uploadFiles",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "FormDataMultiPart",
            "optional": false,
            "field": "multiPart",
            "description": "<p>Données de formulaire contenant les champs:</p> <ul> <li>file: Fichier à envoyer</li> <li>folder: Dossier dans lequel placer les fichiers</li> <li>token-provider: Service de validation des tokens</li> <li>token: Token d'authentification</li> </ul>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success": [
          {
            "group": "Success",
            "optional": false,
            "field": "201_Created",
            "description": "<p>Fichier envoyé avec succès</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "401_Unauthorized",
            "description": "<p>Le token fourni n'a pas pu être validé / Vous n'êtes pas identifié</p>"
          },
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "409_Conflict",
            "description": "<p>Un fichier portant ce nom existe déjà dans le dossier</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/FileService.java",
    "groupTitle": "Files"
  },
  {
    "type": "get",
    "url": "/quicklook/cleanCache",
    "title": "Nettoyer le cache",
    "group": "Quicklook",
    "name": "cleanCache",
    "header": {
      "fields": {
        "Header": [
          {
            "group": "Header",
            "type": "String",
            "optional": false,
            "field": "Authorization",
            "description": "<p>Token d'identification</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "Response",
            "description": "<p>Confirmation de nettoyage du cache</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "optional": false,
            "field": "401_Unauthorized",
            "description": "<p>Vous devez être identifié pour nettoyer le cache</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/QuicklookService.java",
    "groupTitle": "Quicklook"
  },
  {
    "type": "get",
    "url": "/quicklook/:format/collection",
    "title": "Récupération des granules de la collection",
    "group": "Quicklook",
    "name": "getCollectionGranules",
    "examples": [
      {
        "title": "Example-Usage:",
        "content": "http://yourserver.com/quicklook/json/collection?q=searchTerms&parentIdentifier=collectionName&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD",
        "type": "json"
      }
    ],
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "format",
            "description": "<p>Format du fichier à récupérer (json ou xml)</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "q",
            "description": "<p>Filtrage des granules par mots-clés</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "parentIdentifier",
            "defaultValue": "Toutes",
            "description": "<p>Collections auxquelles appartiennent les granules séparées par un espace (&quot;%20&quot; dans une url)</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "startDate",
            "description": "<p>Date de départ</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "endDate",
            "description": "<p>Date de fin</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "Granule",
            "description": "<p>Description de l'ensemble des granules disponibles</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": " HTTP/1.1 200 OK\n\t    {\n\t\t  \"id\": \"responseId\",\n\t\t  \"title\": \"responseTitle\",\n\t\t  \"updated\": \"updatedDate\",\n\t\t  \"authors\": [\n\t\t    \"responseAuthor\"\n\t\t  ],\n\t\t  \"links\": [\n\t\t    \"http://yourserver.com/quicklook/json/collection?parentIdentifier=collectionName&sd=YYYY-MM-DD&ed=YYYY-MM-DD\"\n\t\t  ],\n\t\t  \"totalResults\": 1,\n\t\t  \"granules\": [\n\t\t    {\n\t\t      \"date\": \"YYYY-MM-DDTHH:mm:ssZ\",\n\t\t      \"parentIdentifier\": \"collectionName\",\n\t\t      \"type\": \"granuleType\",\n\t\t      \"media\": {\n\t\t        \"content\": \"granuleImageUrl\",\n\t\t        \"category\": \"granuleMediaCategory\"\n\t\t      }\n\t\t    }\n\t\t  ]\n\t\t}",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error": [
          {
            "group": "Error",
            "optional": false,
            "field": "400_BadRequest",
            "description": "<p>La requête est mal formulée</p>"
          },
          {
            "group": "Error",
            "optional": false,
            "field": "204_NoContent",
            "description": "<p>Aucun granule n'est disponible</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/QuicklookService.java",
    "groupTitle": "Quicklook"
  },
  {
    "type": "get",
    "url": "/quicklook/:format",
    "title": "Récupération du fichier de description des collections",
    "group": "Quicklook",
    "name": "getDescriptionFile",
    "examples": [
      {
        "title": "Example-Usage:",
        "content": "http://yourserver.com/quicklook/json",
        "type": "json"
      }
    ],
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "allowedValues": [
              "json",
              "atom"
            ],
            "optional": true,
            "field": "format",
            "defaultValue": "atom",
            "description": "<p>Format du fichier à récupérer</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "Description",
            "description": "<p>Description de l'ensemble des collections disponibles</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response-JSON:",
          "content": "HTTP/1.1 200 OK\n{\n  \"searchUrl\": \"http://yourserver.com/quicklook/json/collection?q={searchTerms?}&parentIdentifier={eo:parentIdentifier?}&sd={time:start}&ed={time:end?}\",\n  \"totalResults\": 1,\n  \"results\": [\n    \"collectionName\"\n  ],\n  \"details\": [\n    {\n      \"name\": \"collectionName\",\n      \"firstDate\": \"YYYYMMDD\",\n      \"lastDate\": \"YYYYMMDD\",\n      \"properties\": {}\n    }\n  ]\n}",
          "type": "json"
        }
      ]
    },
    "error": {
      "fields": {
        "Error": [
          {
            "group": "Error",
            "optional": false,
            "field": "500_serverError",
            "description": "<p>Une erreur est survenue sur le serveur</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/QuicklookService.java",
    "groupTitle": "Quicklook"
  },
  {
    "type": "get",
    "url": "/quicklook/isAlive",
    "title": "Vérifier la disponibilité du service",
    "group": "Quicklook",
    "name": "isAlive",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "Response",
            "description": "<p>yes</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/fr/aeris/commons/service/QuicklookService.java",
    "groupTitle": "Quicklook"
  }
] });

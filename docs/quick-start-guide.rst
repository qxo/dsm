

Introduction  
============

Declarative Stream Mapping(DSM) is a *stream* deserializer library that makes parsing of **XML and JSON** easy. 
DSM allows you to make custom parsing, filtering, 
transforming, aggregating, grouping on any 
JSON or XML document at stream time(read only once). 
DSM uses yaml or json for configuration definitions 

**If you parsing a complex, huge  file and 
want to have high performance and low memory usage then DSM is for you.**


Simple Example  
===============

**Lets Parse below simple JSON and XML file with DSM**

File contents are taken from `Swagger Petstore example <https://editor.swagger.io/>`_. Slightly changed.

**Source file**

..  content-tabs::
     
     .. tab-container:: tab1
        :title: JSON

        .. code-block:: json

                {
                    "id": 1,
                    "name": "Van Kedisi",
                    "status": "sold",
                    "createDate": "01/24/2019",
                    "category": {"id": 1,"name": "Cats"},
                    "tags": [
                        {"id": 1,"name": "Cute"},
                        {"id": 2,"name": "Popular"}
                    ],
                    "photoUrls": ["url1","url2"	]
                }

     .. tab-container:: tab2
        :title: XML 

        .. code-block:: xml
        
            <?xml version="1.0" encoding="UTF-8" ?>
        
            <Pet id="1">                
                <name>Van Kedisi</name>
                <status>sold</status>
                <createDate>01/24/2019</createDate> 
                <category>
                    <id>1</id>
                    <name>Cats</name>
                </category>
                <tags>
                    <tag>
                        <id>1</id>
                        <name>Cute</name>
                    </tag>
                    <tag>
                        <id>2</id>
                        <name>Popular</name>
                    </tag>
                </tags>
                <photoUrls>
                    <photoUrl>url1</photoUrl>
                    <photoUrl>url2</photoUrl>
                 </photoUrls>
            </Pet>




**Those are rules that we want to apply during parsing**.

- exclude "photoUrls" tag. 
- read only "name" field of "tags" tag. 
- read only  "name" field of "category" tag. 
- **add new the "isPopular" field that it's value is true, if "tag.name" has "Popular" value**


**DSM config file**   



[YAML]

.. code-block:: yaml

    params:
        dateFormat: MM/dd/yyyy    
    result:
       type:object
       path: / 
       xml:
          path: /Pet     
       filter: $self.data.status=='sold'   
       fields:
           id:
             dataType: int
             xml:
                attribute: true
           name: string
           status: status
           createDate: date
           category:
               path: category/name
           isPopular:                
                default: $self.data.tags.contains("Popular")
           tags:
                 type:array
                 path: tags/name |tags/tag/name    # this is a regex expression. works for both JSON and XML
                 
                    
              
**Class to deserialize**

[JAVA]

.. code-block:: java

        public class Pet {
                private int id;
                private String name;
                private boolean isPopular;
                private String status;
                private String category;
                private Date createDate;
                private List<String> tags;
                
                // getter/setter	
        }



**Read Data**

.. code-block:: java       
        
    DSMBuilder builder = new DSMBuilder("dsm-config-file.yaml");    
    DSM dsm = builder.setType(DSMBuilder.XML).create();        
    Pet pet = dsm.toObject(new File("path/to/xmlFile.xml"),Pet.class);  // read data from xml file
    
    dsm = builder.setType(DSMBuilder.JSON).create();        
    pet = dsm.toObject(new File("path/to/jsonFile.json"),Pet.class);  // read data from json file





Features
==============


- **Work** for both **XML** and **JSON** 
- **Custom stream parsing**
- **Filtering** by value on any field with very **low cognitive complexity**
- Flexible value **transformation**. 
- **Default value assignment**
- Custom **function calling** during parsing
- **Powerful Scripting**(`Apache JEXL <https://commons.apache.org/proper/commons-jexl/reference/syntax.html>`_, Groovy, Javascript and other jsr223 implementations are supported)
- **Multiple inheritance** between  DSM config file (DSM file can **extends to another config file**) 
- **Reusable fragments support** 
- Very **short learning curve**
- **Memory** and **CPU** efficient
- **Partial data extraction** from JSON or XML
- **String manipulation** with expression




Installation
==============

..  content-tabs::

    .. tab-container:: tab1
        :title: Maven

        **Jackson**
        
        .. code-block:: xml

            <dependency>
              <groupId>com.github.mfatihercik</groupId>
              <artifactId>dsm</artifactId>
              <version>1.0.3</version>
            </dependency>
    
    .. tab-container:: tab2
        :title: Gradle

        **Jackson**
        
        .. code-block:: xml

            compile ('com.github.mfatihercik:dsm:1.0.3')
            






Sample Config File
===================

Detailed documentation and all option is `here <specification/main.html>`_.

This config file contains some possible option and their short description.

[header.yaml]

.. code-block:: yaml

    params:
        dateFormat: MM/dd/yyyy                 # define date format for "date" data type        
    transformations:
        SOLD_STATUS:                           # value transformation for "isAvailable" property
          map:
             sold: false
             pending: false
             available: true
             DEFAULT: false
        SOLD_STATUS_SKIP:
           $ref:   $transformations.SOLD_STATUS   # extends to "SOLD_STATUS" transformation.
           map:
              DEFAULT: exclude                  # exclude default value 
           onlyIfExist:                         # make transformation only source value exist in transformation map other wise return as it is
    functions:
        insertPet: com.example.InsertPet        # declare a function to declare at Parsing Element.
        
    fragments:                                  # create reusable fragment
        category:
          type:object      
          fields:      
             id: int
             name: string
             type: string
             

[main.yaml]

.. code-block:: yaml
    
    $extends: header.yaml                       # extends to header.yaml config.
    result:
        type:array                          # result is an array
        path: / | /Pets/Pet                 # start reading form beginning for json. path is a regex. we can define both for xml and json same time. or we can declare for xml in XML field.
        xml:
          path: /Pets/Pet                    # start reading from /Pets/Pet for xml
        
        filter: $self.data.isAvailable          # filter by "isAvailable" property. "self" key word refers to current Node. self.parent refers to parent Node. self.data refers to current node data
        
        function: insertPet                     # call "insertPet" function for every element of "result" array
        fields:
          name: string                          # read name as string. 
          id:
            dataType: int                           # read id as int  
            xml:
              attribute: true                   # id is an attribute on /Pets/Pet tag.
          createDate: date                      # use dateFormat in params then convert string to date    
          isAvailable: 
              path: status              # read isAvailable as string from "status" tag
              dataType: boolean
              transformationCode: SOLD_STATUS    # user "SOLD_STATUS" transformation to map from "status" to "isAvailable" 
          category: 
              $ref: $fragments.category          # extends to "fragment.category"
              fields:
                 type: exclude                  # exclude "type" field from "category" fragment
                 name:                            
                     default: 'Animal'            #set default value to 'Animal' if "category/name" tag not exist in source document
          isPopular:
                default: $self.data.tags.contains("Popular")   # set default value of "isPopular" property
                
          tags:
              type:array
              path: tags/name
              filter: $value.length>15      # filter by length of value.
              xml:
                path: tags/tag/name





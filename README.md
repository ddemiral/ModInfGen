# ModInfGen
ModInfGen (Module Info Generator) is a tool written in java that can generate a `module-info.java` file from a *.json config file `module-info.json5`.

But why would you want to use this tool over manually writing your `module-info.java`?
Well, ModInfGen has support for several utilities that make your life easier.

## Unique Features
- module root expansion (e.g. `.` $\rightarrow$ com.example.project)
- automatically injected comments
- better structure than the linear structure of `module-info.java`
- variables

### Module root expansion
Why would you want to type your root module over and over when it could be easier?
Here's an example of the feature in use:

````json5
{
    "module": "com.example.project",
    "exports": [
        ".",    // expanded to "com.example.project"
        ".core" // "com.example.project.core"
    ]
}
````

### Automatically injected comments
You can declare comments which will be automatically injected into the generated `module-info.java`, 
the placement of these comments is determined by the section the comments are assigned to. Below is an example:

````json5
{
    // ...
	
    "comments": {
        "header": "This is a test header comment",
        "footer": "This is the closing comment",
        "requires": "Dependencies are below",
        "exports": "These modules are exported",
        "opens": "These modules are opened to reflection", 
        "legacy": "These are the legacy lines"
    }
    
    // ...
}
````

Details about the sections can be found in this documentation.

### Variables
Variables can be declared in the `variables` section and can be used anywhere in `module-info.json5` except for the `variables` section.

Variable names must follow these rules to be considered valid:
- must start with $
- following the $ must be a lowercase letter (a-z)
- following that can be any of the following characters: 
  - a-z and A-Z
  - 0-9
  - underscores _ and hyphens -

Example:
````json5
{
    "variables": {
        "$gson": "com.google.gson"
    },
    
    "requires": [
        "$gson"
    ]
}
````

## The file `module-info.json5`
`module-info.json5` is the file that essentially replaces `module-info.java`, as you can generate that file from your config in `module-info.json5`.
If you are unfamiliar with *.json5-files, it's essentially JSON but with comments, which is the reason why it's used over plain JSON.
The following fields can/must be declared in the config:
- ``module``: The root module (e.g. `com.example.project`)
- ``source-root``: The path to your source directory (e.g. `src/main/java`, optional)
- ``variables {}``: Contains the key-value mappings for variable names to their value (optional)
- ``exports []``: Contains a list of modules that are required by your project (optional)
- ``opens []``: A list of declarations that exposes your packages to java's reflection for specified dependencies (optional)
- ``legacy []``: A list of lines that follow the syntax of `module-info.java`, these lines are not validated by ModInfGen and are appended to the generated `module-info.java`
- ``comments {}``: The comment mappings, supported keys are: header, footer, requires, exports, opens and legacy.


...WIP (further documentation coming in the future)...
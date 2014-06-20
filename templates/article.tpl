CategoryPath:{{# treepath }}
  - "{{ name }}"{{/ treepath }}

Names:{{# names }}
  - {{ lang }}: "{{ name }}"{{/ names }}

Categories:{{# allcategories }}
  - {{ lang }}:{{# categories }}
    - "{{name}}"{{/ categories }}{{/ allcategories }}


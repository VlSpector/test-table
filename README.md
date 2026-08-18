### Task
The application will consist of 2 screens:

First screen:
Two fields in which we enter the number of rows and columns. (maximum limit - 6 columns and 1000 rows)


Second screen: 
We build a table, the size of which is specified on the first screen. We load random data into this table (data format is string format).
Single click on a cell should change the color of the cell (one click changes the color to green, another click returns the color back).  Double click allows to change the data in the cell. 

Technical requirements: Only for tablets, Jetpack Compose, Modularization (UI, Domain, Data. Also, random data is taken from the data layer).


### Notes tom implementation
- Since it was requested to use mocked data, MockedTableApi was introduced to simulate BE bahviour
- No data persistence was requested so no Room added, user edits are cached in MockedTableApi as if edits were sent to BE
- The Table rows count is implemented via passing limit to the MockedTableApi
- Since the rows limit is up to 1000, the rows are paged via Paging3 api
- The same table data is shared across all table sizes, so changing a cell in 4x4 size will be visible on 6x1000 size
- Since there is no real BE and no Local DB, there are no DTO models and the MockedTableApi returns Domain models without DTO - Domain mappings anywhere as it would be 1 to 1 mapping for the no real reason
- User's input for rows/columns was coerceIn required values with no proper validation UX for simplicity
- UI is for Tablets only, no adaptions for phone/tablet use


### Architecture 
- Multi-module app (from requirements)
  - app module
  - common module (for shared code, utils, helpers ect, only SuspendResult for now)
  - presentation (ui) module
  - domain module
  - data module
- MVI
- Compose UI
- Hilt for DI
- Compose Navigation3 for navigation


This document explains the policies specified in PolicyFile.json. Each policy restricts access to certain combinations of attributes to protect sensitive data and prevent potential re-identification risks.
________________


Understanding Policy Structure
Each policy in PolicyFile.json follows a structure like:
Q() :- Entity.attribute(x), Entity.attribute(y) .
These policies define restrictions on accessing multiple attributes together, preventing potential privacy violations. Below, we describe each policy in detail.
________________


Policy Explanations
* Q() :- Patient.gender(x), Patient.address(x, y) .

   * Description: Denies access to combinations of gender and address of a patient.
   * Reasoning: Combining gender and address details can significantly increase the risk of patient re-identification.
   * Q() :- MedicationStatement.subject(x, y), link(x, y) .

      * Description: Denies access to MedicationStatement subjects that are linked together.
      * Reasoning: Prevents cross-referencing medication records with linkage information, which might reveal patterns in treatment history.
      * Q() :- Patient.gender(x, y), Patient.address(x, z) .

         * Description: Denies access to gender and address when retrieved together for the same patient.
         * Reasoning: Ensures that gender details are not linked to geographic location.
         * Q() :- MedicationStatement.subject(x, y), link(y, z) .

            * Description: Restricts access to medication subjects when linked with another entity.
            * Reasoning: Helps prevent tracking medication history across linked datasets.
            * Q() :- Patient_id(x), Patient.generalPractitioner(x, y) .

               * Description: Prevents accessing patient IDs along with their assigned general practitioner.
               * Reasoning: Direct linkage between patient IDs and doctors could expose care relationships.
               * Q() :- Condition.subject(x, y), link(y, z) .

                  * Description: Restricts access to condition subjects when linked to another entity.
                  * Reasoning: Helps prevent correlation of medical conditions through linked datasets.
                  * Q() :- Procedure.code(x, y), Procedure.performedDateTime(x, z) .

                     * Description: Prevents accessing procedure codes alongside procedure dates.
                     * Reasoning: Reduces the risk of inferring when specific medical procedures were performed.
                     * Q() :- Observation.valueQuantity(x, y), Quantity.value(y, z), value(z, v) .

                        * Description: Prevents accessing observed quantity values and their derived values.
                        * Reasoning: Protects against exposure of sensitive health metrics.
                        * Q() :- Practitioner.qualification(x, y), Practitioner.qualification.code(y, z) .

                           * Description: Restricts access to practitioner qualifications and their codes together.
                           * Reasoning: Helps prevent detailed profiling of medical professionals.
                           * Q() :- Encounter.type(x, y), Encounter.status(x, z) .

                              * Description: Denies access to encounter types along with their statuses.
                              * Reasoning: Protects against inferring the status of medical encounters.
                              * Q() :- Location.name(x, y), Location.address(x, z) .

                                 * Description: Restricts access to location names and addresses together.
                                 * Reasoning: Prevents detailed location identification that could expose facility details.
                                 * Q() :- Practitioner.birthDate(x, y), Practitioner.identifier(x, z), Practitioner.qualification(x, w) .

                                    * Description: Denies access to a combination of practitioner birth date, identifier, and qualification.
                                    * Reasoning: Protects against profiling and unauthorized personal data exposure.
                                    * Q() :- Procedure.status(x, y), Procedure.encounter(x, u) .

                                       * Description: Restricts access to procedure statuses and the corresponding encounters.
                                       * Reasoning: Prevents tracking of completed medical procedures based on encounters.
Note: The policies defined above can be applied individually or in combination, depending on specific privacy requirements. Users implementing these policies should assess their particular needs and select the appropriate set of restrictions to ensure compliance with data protection standards and minimize the risk of sensitive data exposure.

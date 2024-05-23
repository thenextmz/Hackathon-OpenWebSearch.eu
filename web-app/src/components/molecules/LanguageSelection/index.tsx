import { languageIndices } from '@/app/utils/globals';
import { getDisplayString } from '@/app/utils/helper';
import { IndexEntry } from '@/types/indexEntry';
import { QueryParams } from '@/types/queryParams';
import { CheckIcon } from '@chakra-ui/icons';
import { Accordion, AccordionButton, AccordionIcon, AccordionItem, AccordionPanel, Box } from '@chakra-ui/react';
import { FC } from 'react';

interface LanguageSelectionProps {
  queryParams: QueryParams;
  setQueryParams: (queryParams: QueryParams) => void;
}

export const LanguageSelection: FC<LanguageSelectionProps> = ({ queryParams, setQueryParams }) => {
  return (
    <Accordion className="rounded-xl flex-1" allowMultiple={false} allowToggle={true}>
      <AccordionItem className="rounded-xl bg-white">
        <AccordionButton>
          <Box as="span" flex="1" textAlign="left">
            <p className="font-bold">Langauge : {queryParams.language ? getDisplayString(queryParams.language, languageIndices) : languageIndices.default.displayString}</p>
          </Box>
          <AccordionIcon />
        </AccordionButton>

        <AccordionPanel
          onClick={() => {
            // Remove index from queryParams if it is default
            const newDictionary = { ...queryParams };
            delete newDictionary['language'];
            setQueryParams(newDictionary);
          }}
        >
          <div className="flex flex-row items-center gap-2">
            <p className={!queryParams.language ? 'font-bold' : ''}>{languageIndices.default.displayString}</p>
            {!queryParams.language && <CheckIcon />}
          </div>
        </AccordionPanel>

        <AccordionPanel
          onClick={() => {
            setQueryParams({ ...queryParams, language: languageIndices.english.apiString });
          }}
        >
          <div className="flex flex-row items-center gap-2">
            <p className={queryParams.language == languageIndices.english.apiString ? 'font-bold' : ''}>{languageIndices.english.displayString}</p>
            {queryParams.language == languageIndices.english.apiString && <CheckIcon />}
          </div>
        </AccordionPanel>

        <AccordionPanel
          onClick={() => {
            setQueryParams({ ...queryParams, language: languageIndices.german.apiString });
          }}
        >
          <div className="flex flex-row items-center gap-2">
            <p className={queryParams.language == languageIndices.german.apiString ? 'font-bold' : ''}>{languageIndices.german.displayString}</p>
            {queryParams.language == languageIndices.german.apiString && <CheckIcon />}
          </div>
        </AccordionPanel>
      </AccordionItem>
    </Accordion>
  );
};

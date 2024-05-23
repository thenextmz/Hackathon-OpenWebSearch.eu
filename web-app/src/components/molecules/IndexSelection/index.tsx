import { indexIndices } from '@/app/utils/globals';
import { getDisplayString } from '@/app/utils/helper';
import { IndexEntry } from '@/types/indexEntry';
import { QueryParams } from '@/types/queryParams';
import { CheckIcon } from '@chakra-ui/icons';
import { Accordion, AccordionButton, AccordionIcon, AccordionItem, AccordionPanel, Box } from '@chakra-ui/react';
import { FC } from 'react';

interface IndexSelectionProps {
  queryParams: QueryParams;
  setQueryParams: (queryParams: QueryParams) => void;
}

export const IndexSelection: FC<IndexSelectionProps> = ({ queryParams, setQueryParams }) => {
  return (
    <Accordion className="rounded-xl flex-1" allowMultiple={false} allowToggle={true}>
      <AccordionItem className="rounded-xl bg-white ">
        <AccordionButton>
          <Box as="span" flex="1" textAlign="left">
            <p className="font-bold">Index : {queryParams.index ? getDisplayString(queryParams.index, indexIndices) : indexIndices.default.displayString}</p>
          </Box>
          <AccordionIcon />
        </AccordionButton>

        <AccordionPanel
          onClick={() => {
            // Remove index from queryParams if it is default
            const newDictionary = { ...queryParams };
            delete newDictionary['index'];
            setQueryParams(newDictionary);
          }}
        >
          <div className="flex flex-row items-center gap-2">
            <p className={!queryParams.index ? 'font-bold' : ''}>{indexIndices.default.displayString}</p>
            {!queryParams.index && <CheckIcon />}
          </div>
        </AccordionPanel>

        <AccordionPanel
          onClick={() => {
            setQueryParams({ ...queryParams, index: indexIndices.demoSimpleWiki.apiString });
          }}
        >
          <div className="flex flex-row items-center gap-2">
            <p className={queryParams.index == indexIndices.demoSimpleWiki.apiString ? 'font-bold' : ''}>{indexIndices.demoSimpleWiki.displayString}</p>
            {queryParams.index == indexIndices.demoSimpleWiki.apiString && <CheckIcon />}
          </div>
        </AccordionPanel>

        <AccordionPanel
          onClick={() => {
            setQueryParams({ ...queryParams, index: indexIndices.demoUnisGraz.apiString });
          }}
        >
          <div className="flex flex-row items-center gap-2">
            <p className={queryParams.index == indexIndices.demoUnisGraz.apiString ? 'font-bold' : ''}>{indexIndices.demoUnisGraz.displayString}</p>
            {queryParams.index == indexIndices.demoUnisGraz.apiString && <CheckIcon />}
          </div>
        </AccordionPanel>

        {/*<AccordionPanel
          onClick={() => {
            setQueryParams({ ...queryParams, index: indices.dlrPrototype.apiString });
          }}
        >
          <div className="flex flex-row items-center gap-2">
            <p className={queryParams.index == indices.dlrPrototype.apiString ? 'font-bold' : ''}>{indices.dlrPrototype.displayString}</p>
            {queryParams.index == indices.dlrPrototype.apiString && <CheckIcon className="" />}
          </div>
        </AccordionPanel>*/}
      </AccordionItem>
    </Accordion>
  );
};

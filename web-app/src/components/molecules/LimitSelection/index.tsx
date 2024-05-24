import { IndexEntry } from '@/types/indexEntry';
import { QueryParams } from '@/types/queryParams';
import { CheckIcon } from '@chakra-ui/icons';
import { Accordion, AccordionButton, AccordionIcon, AccordionItem, AccordionPanel, Box } from '@chakra-ui/react';
import { FC, useState } from 'react';

interface LimitSelectionProps {
  queryParams: QueryParams;
  setQueryParams: (queryParams: QueryParams) => void;
}

export const LimitSelection: FC<LimitSelectionProps> = ({ queryParams, setQueryParams }) => {
  const defaulString = 'Default/20';
  const limits = [10, 50, 1000];
  const [limitState, setLimitState] = useState<number | undefined>();

  return (
    <Accordion className="rounded-xl flex-1" allowMultiple={false} allowToggle={true}>
      <AccordionItem className="rounded-xl bg-white">
        <AccordionButton>
          <Box as="span" flex="1" textAlign="left">
            <p className="font-bold text-sm">Limit: {queryParams.limit ? limitState : defaulString}</p>
          </Box>
          <AccordionIcon />
        </AccordionButton>

        <AccordionPanel
          onClick={() => {
            // Remove index from queryParams if it is default
            const newDictionary = { ...queryParams };
            delete newDictionary['limit'];
            setLimitState(undefined);
            setQueryParams(newDictionary);
          }}
        >
          <div className="flex flex-row items-center gap-2">
            <p className={!queryParams.limit ? 'font-bold text-sm' : 'text-sm'}>{defaulString}</p>
            {!queryParams.limit && <CheckIcon />}
          </div>
        </AccordionPanel>

        {limits.map((limit, limitIndex) => {
          return (
            <AccordionPanel
              key={limitIndex}
              onClick={() => {
                setQueryParams({ ...queryParams, limit: limit });
                setLimitState(limit);
              }}
            >
              <div className="flex flex-row items-center gap-2">
                <p className={queryParams.limit == limit ? 'font-bold text-sm' : 'text-sm'}>{limit}</p>
                {queryParams.limit == limit && <CheckIcon />}
              </div>
            </AccordionPanel>
          );
        })}
      </AccordionItem>
    </Accordion>
  );
};

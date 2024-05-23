'use client';

import ApiClient from '@/app/utils/api';
import { FullText } from '@/types/fullText';
import { Result } from '@/types/queryParams';
import { ChevronRightIcon } from '@chakra-ui/icons';
import { Button, Drawer, DrawerBody, DrawerCloseButton, DrawerContent, DrawerFooter, DrawerHeader, DrawerOverlay, Input, Spinner, useDisclosure } from '@chakra-ui/react';
import { FC, RefObject, useRef, useState } from 'react';
import { FocusableElement } from '@chakra-ui/utils';

interface FullTextSectionProps {
  item: Result;
}

export const FullTextSection: FC<FullTextSectionProps> = ({ item }) => {
  const api = new ApiClient();
  const [loading, setLoading] = useState(false);
  const [fullText, setFullText] = useState<FullText>(); // Full text state for displaying the full text
  const [buttonPressed, setButtonPressed] = useState<string>('');

  const { isOpen, onOpen, onClose } = useDisclosure();

  const getFullText = async () => {
    setLoading(true);
    setButtonPressed('fullText');
    const data: FullText = await api.fullText(item.id);
    console.log(data.fullText);
    setFullText(data);
    setLoading(false);
    onOpen();
  };

  const getAISummary = async () => {
    setLoading(true);
    setButtonPressed('aiSummary');
    const data: FullText = await api.fullText(item.id);
    const aiSummary = 'TODO: Implement AI Summary of Full Text';
    setFullText({ ...data, aiSummary: aiSummary });
    setLoading(false);
    onOpen();
  };

  return (
    <div>
      <Button
        className="ml-2 gap-2"
        colorScheme="teal"
        size="xs"
        onClick={() => {
          getFullText();
        }}
      >
        {loading && buttonPressed === 'fullText' ? <Spinner size={'xs'} /> : <ChevronRightIcon />}
        Get Full Text
      </Button>

      <Button
        className="ml-2 gap-2"
        colorScheme="teal"
        size="xs"
        onClick={() => {
          getAISummary();
        }}
      >
        {loading && buttonPressed === 'aiSummary' ? <Spinner size={'xs'} /> : <ChevronRightIcon />}
        AI Summary of Full Text
      </Button>

      {buttonPressed === 'fullText' && fullText?.fullText && (
        <Drawer isOpen={isOpen} placement="right" onClose={onClose} size={'lg'}>
          <DrawerOverlay />
          <DrawerContent>
            <DrawerCloseButton />
            <DrawerHeader>Full Text</DrawerHeader>
            <DrawerBody className="bg-themeDark">
              <p className="text-md text-white">{fullText.fullText}</p>
            </DrawerBody>
          </DrawerContent>
        </Drawer>
      )}

      {buttonPressed === 'aiSummary' && fullText?.aiSummary && (
        <Drawer isOpen={isOpen} placement="right" onClose={onClose} size={'lg'}>
          <DrawerOverlay />
          <DrawerContent>
            <DrawerCloseButton />
            <DrawerHeader>AI Summary</DrawerHeader>
            <DrawerBody className="bg-themeDark">
              <p className="text-md text-white">{fullText.aiSummary}</p>
            </DrawerBody>
          </DrawerContent>
        </Drawer>
      )}
    </div>
  );
};

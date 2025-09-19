/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import { PopconfirmDeleteBtn } from '@/components/CallBackButton/PopconfirmDeleteBtn';
import { l } from '@/utils/intl';
import { DeliveredProcedureOutlined } from '@ant-design/icons';
import { Skeleton, Space, Tag, Tooltip, Timeline, Typography } from 'antd';

const { Text } = Typography;

export type GitLogItem = {
  id: number;
  message: string;
  date?: string;
};

export interface GitLogListProps {
  data: GitLogItem[];
  onSelectListen?: (value: GitLogItem) => void;
  onDeleteListen?: (value: GitLogItem) => void;
  onRollBackListen?: (value: GitLogItem) => void;
  loading?: boolean;
  header?: string;
}

const GitLogList = (props: GitLogListProps) => {
  const {
    data,
    loading
  } = props;

  return (
    <div>
      <Skeleton loading={loading} active>
        <Timeline mode="left" style={{ marginTop: 16 }}>
          {data?.map((item) => (
            <Timeline.Item
              key={item.id}
              color='blue'
              style={{ cursor: 'pointer' }}
            >
              <Space direction="vertical" size={4} style={{ width: '100%' }}>
                <Text strong>{item.id}</Text>
                <Text>{item.message}</Text>
                {item.date && <Text type="secondary">{item.date}</Text>}
              </Space>
            </Timeline.Item>
          ))}
        </Timeline>
      </Skeleton>
    </div>
  );
};

export default GitLogList;

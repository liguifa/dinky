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

import { API_CONSTANTS } from '@/services/endpoints';
import React from 'react';
import { Card } from 'antd';
import GitLogList from '@/components/GitLogList';
import { useRequest } from '@umijs/max';

export const GitLog = (props: {
  taskId: number;
}) => {
  const { taskId } = props;

  const { data, refresh, loading } = useRequest(
    {
      url: `${API_CONSTANTS.GET_JOB_GITLOG}/${taskId}`
    }
  );

  return (
    <Card>
      <GitLogList
        loading={loading}
        data={data}
      />
    </Card>
  );
};
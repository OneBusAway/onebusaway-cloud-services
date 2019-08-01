/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.cloud.aws;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.util.EC2MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AwsLeadershipElectionService {

    private static Logger _log = LoggerFactory.getLogger(AwsLeadershipElectionService.class);

    private boolean _primary = true;

    private ScheduledExecutorService _scheduledExecutorService;

    public boolean isInstancePrimary() {
        String autoScalingGroupName = System.getProperty("aws.autoScalingGroup");
        if (autoScalingGroupName == null) {
            return true;
        }
        scheduleTaskIfUnscheduled(autoScalingGroupName);
        return _primary;
    }

    private void scheduleTaskIfUnscheduled(String autoScalingGroupName) {
        if (_scheduledExecutorService == null) {
            _log.info("scheduling primary check...");
            _scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            _scheduledExecutorService.scheduleAtFixedRate(new LeadershipElectionTask(autoScalingGroupName),
                    1, 1, TimeUnit.MINUTES);
            // and run now, synchronously:
            new LeadershipElectionTask(autoScalingGroupName).run();
        }
    }

    private class LeadershipElectionTask implements Runnable {
        private AmazonAutoScaling _autoScale;
        private AmazonEC2 _ec2;
        private String _autoScalingGroupName;

        public LeadershipElectionTask(String autoScalingGroupName) {
            try {
                _ec2 = AmazonEC2ClientBuilder.standard().build();
                _autoScale = AmazonAutoScalingClientBuilder.standard().build();;
                _autoScalingGroupName = autoScalingGroupName;
            } catch(Exception e){
                _log.warn("Unable to create AWS Clients", e);
            }
        }

        public void run() {
            AutoScalingGroup autoScalingGroup = getAutoScalingGroups();

            if(autoScalingGroup != null) {
                try {
                    String oldestInstance = null;
                    Date oldestInstanceLaunchTime = new Date();

                    List<String> instanceIds = autoScalingGroup.getInstances().stream()
                            .map(com.amazonaws.services.autoscaling.model.Instance::getInstanceId)
                            .collect(Collectors.toList());

                    List<Instance> instances = getInstances(instanceIds);

                    for (Instance instance : instances) {
                        if (instance.getLaunchTime().before(oldestInstanceLaunchTime)) {
                            oldestInstanceLaunchTime = instance.getLaunchTime();
                            oldestInstance = instance.getInstanceId();
                        }
                    }

                    if (oldestInstance != null && oldestInstance.equals(EC2MetadataUtils.getInstanceId())) {
                        _log.warn("This is the primary instance.");
                        _primary = true;
                    } else {
                        _log.warn("This is not the primary instance. Oldest Instance Id is {}, this Instance Id is {}", oldestInstance, EC2MetadataUtils.getInstanceId());
                        _primary = false;
                    }
                } catch (Exception any) {
                    _log.error("exception with primary check:" + any, any);
                }
            } else {
                _log.warn("Not the primary instance, no autoScaling group found.");
                _primary = false;
            }
        }

        private AutoScalingGroup getAutoScalingGroups(){
            try {
                DescribeAutoScalingGroupsResult result = _autoScale.describeAutoScalingGroups(
                        new DescribeAutoScalingGroupsRequest());
                return result.getAutoScalingGroups().stream()
                        .filter(group -> group.getAutoScalingGroupName().startsWith(_autoScalingGroupName))
                        .findFirst().orElse(null);
            } catch (Exception any) {
                _log.error("exception retrieving autoscaling groups " + any, any);
            }
            return null;
        }

        private List<Instance> getInstances(List<String> instanceIds){
            try {
                DescribeInstancesRequest request = new DescribeInstancesRequest();
                request.setInstanceIds(instanceIds);
                DescribeInstancesResult result = _ec2.describeInstances(request);
                List<Instance> instances = new ArrayList();
                for (Reservation reservation : result.getReservations()) {
                    instances.addAll(reservation.getInstances());
                }
                return instances;
            } catch (Exception e){
                _log.error("Unable to retreive instances", e);
                return Collections.EMPTY_LIST;
            }
        }
    }
}

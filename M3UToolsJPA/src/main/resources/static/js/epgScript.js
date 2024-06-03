// script.js
document.addEventListener('DOMContentLoaded', () => {
    
           
    /* const epgData = [
        {
            channel: 'Channel 1',
            programs: [
                { startTime: '08:00', endTime: '09:00', title: 'Morning Show' },
                { startTime: '09:00', endTime: '10:30', title: 'Cartoons' },
                { startTime: '10:30', endTime: '12:00', title: 'News' },
                { startTime: '12:00', endTime: '14:00', title: 'Movie: Adventure Time' }
            ]
        },
        {
            channel: 'Channel 2',
            programs: [
                { startTime: '08:30', endTime: '09:30', title: 'Yoga Class' },
                { startTime: '09:30', endTime: '10:00', title: 'Cooking Show' },
                { startTime: '10:00', endTime: '11:00', title: 'Travel Documentary' },
                { startTime: '11:00', endTime: '12:00', title: 'Talk Show' }
            ]
        },
        {
            channel: 'Channel 3',
            programs: [
                { startTime: '08:00', endTime: '09:00', title: 'Fitness Hour' },
                { startTime: '09:00', endTime: '11:00', title: 'Live Sports' },
                { startTime: '11:00', endTime: '12:30', title: 'News' },
                { startTime: '12:30', endTime: '14:00', title: 'Movie: Comedy Special' }
            ]
        }
    ]; */

	const epgData = [
		{ 'channel': '[UK] BBC 1 4K ◉ rec',
      	'programmes': [
        	{ 'startTime': '16:45', 'endTime': '17:30', 'title': 'Garden Rescue' },
        	{ 'startTime': '17:30', 'endTime': '18:15', 'title': 'New: The Finish Line' },
        	{ 'startTime': '18:15', 'endTime': '19:00', 'title': 'New: Pointless' }
        ]
    	},
    	{ 'channel': '[UK] BBC 2 4K ◉ rec',
      	'programmes': [
        	{ 'startTime': '16:30',  'endTime': '17:15', 'title': 'The Farmers Country Showdown' },
        	{ 'startTime': '17:15',  'endTime': '18:15', 'title': 'Hidden Kingdoms' }
      	]
    	}
		];
		    	
    const epgListContainer = document.getElementById('epg-list');

    // Convert time to minutes from start of the day
    function timeToMinutes(time) {
        const [hours, minutes] = time.split(':').map(Number);
        return hours * 60 + minutes;
    }

    // Calculate duration in minutes
    function calculateDuration(startTime, endTime) {
        return timeToMinutes(endTime) - timeToMinutes(startTime);
    }

    // Create EPG items for each channel
    epgData.forEach(channel => {
        const channelContainer = document.createElement('div');
        channelContainer.className = 'channel-container';

        const channelTitle = document.createElement('div');
        channelTitle.className = 'channel-title';
        channelTitle.textContent = channel.channel;

        const epgList = document.createElement('div');
        epgList.className = 'epg-list';

        channel.programmes.forEach(program => {
            const epgItem = document.createElement('div');
            epgItem.className = 'epg-item';
            const duration = calculateDuration(program.startTime, program.endTime);

            const epgTime = document.createElement('div');
            epgTime.className = 'epg-time';
            // epgTime.textContent = `${program.startTime} - ${program.endTime}`;
            epgTime.textContent = `${program.startTime}`;

            const epgTitle = document.createElement('div');
            epgTitle.className = 'epg-title';
            epgTitle.textContent = program.title;
            epgTitle.style.width = `${duration * 8}px`; // Adjust scaling factor as needed

            epgItem.appendChild(epgTime);
            epgItem.appendChild(epgTitle);

            epgList.appendChild(epgItem);
        });

        channelContainer.appendChild(channelTitle);
        channelContainer.appendChild(epgList);
        epgListContainer.appendChild(channelContainer);
    });
});

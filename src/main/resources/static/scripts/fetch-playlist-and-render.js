async function getPlaylist() {
    try {
      const response = await fetch(
        '/playlist',
        {
          method: 'GET',
        },
      );

      if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Failed to fetch playlist:', error);
      throw error;
    }
  }

  function getFormatBadge(format, requiresTranscoding) {
    const nativeFormats = ['MP4', 'WEBM', 'OGG'];
    const isNative = nativeFormats.includes(format.toUpperCase());
    
    if (isNative) {
      return `<span style="background-color: #28a745; color: white; padding: 2px 8px; border-radius: 3px; font-size: 11px;">${format}</span>`;
    } else {
      return `<span style="background-color: #ffc107; color: black; padding: 2px 8px; border-radius: 3px; font-size: 11px;">${format} → MP4</span>`;
    }
  }

  async function renderPlaylist() {
    try {
      const data = await getPlaylist();

      let table = '<table style="border-collapse: collapse; width: 100%;">';

      table += `
        <thead>
          <tr style="background-color: #f0f0f0;">
            <th style="padding: 10px; text-align: left;">Файл</th>
            <th style="padding: 10px;">Формат</th>
          </tr>
        </thead>

        <tbody>`;

      data.forEach(video => {
        const transcodingHint = video.requiresTranscoding 
          ? ' (требуется транскодировка)' 
          : '';
        const rowStyle = video.requiresTranscoding 
          ? 'style="background-color: #fffde7;"' 
          : '';
        
        table += `
          <tr ${rowStyle} style="border-bottom: 1px solid #ddd;">
            <td style="padding: 10px;">
              <a href="${video.url}" style="text-decoration: none; color: #0066cc;">${video.title}</a>
              <span style="color: #666; font-size: 12px; margin-left: 8px;">${transcodingHint}</span>
            </td>
            <td style="padding: 10px; text-align: center;">
              ${getFormatBadge(video.format, video.requiresTranscoding)}
            </td>
          </tr>`;
      });

      table += `
        </tbody>
      </table>`;

      const container = document.getElementById('container');
      container.innerHTML = table;
    } catch (error) {
      const container = document.getElementById('container');
      container.innerHTML = '<p style="color: red;">Ошибка загрузки плейлиста</p>';
    }
  }

  renderPlaylist();
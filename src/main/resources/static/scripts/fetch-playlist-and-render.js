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

  async function renderPlaylist() {
    try {
      const data = await getPlaylist();

      let table = '<table style="border-collapse: collapse;">';

      table += `
        <thead>
          <tr>
            <th>Доступные файлы</th>
          </tr>
        </thead>

        <tbody>`;

      data.forEach(video => {
        table += `
          <tr>
            <td><a href="${video.url}">${video.title}</a></td>
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